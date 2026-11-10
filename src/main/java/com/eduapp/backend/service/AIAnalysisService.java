package com.eduapp.backend.service;

import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.model.AIAnalysis;
import com.eduapp.backend.repository.AIAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates AI marking of a submitted attempt. NFR-TXN-1: this method is {@code @Async} but
 * NOT {@code @Transactional} — the slow Gemini call runs OUTSIDE any DB transaction. The two
 * transactional touches (load prompt, persist result) are delegated to
 * {@link MarkingPersistenceService} (a separate bean, so the proxy actually starts the
 * transactions and connections are never pinned across the network call).
 *
 * <p>SCALE-MARK-1: the Gemini call is wrapped in {@link AiCallGuard} (bulkhead + circuit breaker).
 * NFR-PARTIAL-1: the response is parsed defensively; partial/malformed output becomes a retryable
 * error instead of a silently-wrong score.
 */
@Service
public class AIAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${gcp.project-id:}")
    private String gcpProjectId;

    @Value("${gcp.location:asia-south1}")
    private String gcpLocation;

    @Value("${gcp.credentials-path:}")
    private String gcpCredentialsPath;

    private final AIAnalysisRepository aiAnalysisRepository;
    private final MarkingPersistenceService persistence;
    private final AiCallGuard aiCallGuard;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIAnalysisService(AIAnalysisRepository aiAnalysisRepository,
            MarkingPersistenceService persistence,
            AiCallGuard aiCallGuard) {
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.persistence = persistence;
        this.aiCallGuard = aiCallGuard;
        // WP-A0.6: bound the marking HTTP call so it fails fast instead of holding a thread forever.
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(10));
        factory.setReadTimeout(java.time.Duration.ofSeconds(60));
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    @Async
    public void analyzeAttempt(StudentPaperAttempt attempt) {
        Long attemptId = attempt.getId();
        logger.info("Starting AI analysis for attempt ID: {}", attemptId);
        try {
            // 1) short read txn — render the prompt while associations are attached
            String prompt = persistence.buildPromptForAttempt(attemptId);

            // 2) slow external call — OUTSIDE any DB transaction, guarded by bulkhead + breaker
            String json = aiCallGuard.execute(() -> callGeminiApi(prompt));

            // 3) defensive parse (no DB)
            json = sanitizeControlCharacters(json);
            json = sanitizeJsonForLatex(json);
            MarkingResult result = parseMarkingResult(json, objectMapper);

            // 4) short write txn — upsert results (throws on partial coverage → retried)
            persistence.persistMarking(attemptId, result);

            logger.info("AI analysis completed and saved for attempt ID: {}", attemptId);
        } catch (Exception e) {
            logger.error("Error during AI analysis for attempt ID: {}", attemptId, e);
            persistence.recordError(attemptId, toUserFacingError(e));
        }
    }

    private static String toUserFacingError(Exception e) {
        if (e instanceof org.springframework.web.client.HttpClientErrorException.TooManyRequests) {
            return "AI Service busy (Rate Limit Exceeded). Please retry in a few moments.";
        }
        if (e instanceof AiUnavailableException) {
            return "AI Service is temporarily busy. We'll retry your marking automatically.";
        }
        String msg = e.getMessage();
        return (msg == null || msg.isBlank()) ? "AI analysis failed. Please retry." : msg;
    }

    /**
     * SEC-3: clamp an AI-returned mark into [0, maxMarks]. The student answer is untrusted input
     * that reaches the prompt, so the model's number is never trusted directly.
     */
    static int clampMarks(int rawMarks, int maxMarks) {
        int safeMax = Math.max(0, maxMarks);
        return Math.max(0, Math.min(rawMarks, safeMax));
    }

    /**
     * NFR-PARTIAL-1: parse the (already sanitized) marking JSON defensively. Only entries with a
     * valid numeric {@code questionId} AND {@code marksAwarded} are retained; malformed entries are
     * dropped (logged) so {@code persistMarking} detects incomplete coverage and retries instead of
     * NPE-ing or under-counting. Throws if the payload is not parseable JSON (also retryable).
     */
    static MarkingResult parseMarkingResult(String json, ObjectMapper mapper) throws IOException {
        JsonNode root = mapper.readTree(json); // throws on non-JSON

        Map<Long, MarkingResult.QuestionMark> byQuestion = new LinkedHashMap<>();
        JsonNode questions = root.path("questions");
        if (questions.isArray()) {
            for (JsonNode q : questions) {
                JsonNode idNode = q.path("questionId");
                JsonNode marksNode = q.path("marksAwarded");
                if (!idNode.canConvertToLong() || !marksNode.canConvertToInt()) {
                    logger.warn("Skipping malformed marking entry (questionId/marksAwarded missing or non-numeric): {}", q);
                    continue;
                }
                long qId = idNode.asLong();
                int rawMarks = marksNode.asInt();
                String feedback = q.path("feedback").asText("");
                byQuestion.put(qId, new MarkingResult.QuestionMark(qId, rawMarks, feedback));
            }
        } else {
            logger.warn("Marking response has no 'questions' array");
        }

        String overallFeedback = root.path("overallFeedback").isTextual()
                ? root.get("overallFeedback").asText() : null;
        Integer aiTotalMarks = root.path("totalMarks").canConvertToInt()
                ? root.get("totalMarks").asInt() : null;

        return new MarkingResult(byQuestion, overallFeedback, aiTotalMarks);
    }

    private String getAccessToken() throws IOException {
        GoogleCredentials credentials;
        if (gcpCredentialsPath != null && !gcpCredentialsPath.isEmpty()
                && !gcpCredentialsPath.equals("/path/to/your/service-account-key.json")) {
            credentials = GoogleCredentials.fromStream(new FileInputStream(gcpCredentialsPath))
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        } else {
            credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        }
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    /**
     * Calls Vertex AI. Runs under {@link AiCallGuard} (see {@link #analyzeAttempt}). Wraps checked
     * exceptions in a RuntimeException so the guard's failure accounting (and the breaker) sees them.
     */
    private String callGeminiApi(String prompt) {
        if (gcpProjectId == null || gcpProjectId.isEmpty() || gcpProjectId.equals("YOUR_GCP_PROJECT_ID")) {
            logger.warn("GCP Project ID is not configured. Skipping actual API call.");
            throw new IllegalStateException("AI Analysis failed: GCP Project ID not configured.");
        }

        String url = String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                gcpLocation, gcpProjectId, gcpLocation, model);
        logger.info("Calling Vertex AI with model: {} in {}", model, gcpLocation);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            headers.setBearerAuth(getAccessToken());
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain Vertex AI access token", e);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.error("Vertex AI call failed with status: {}", response.getStatusCode());
            throw new RuntimeException("Vertex AI call failed with status: " + response.getStatusCode());
        }

        logger.info("Vertex AI call successful");
        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            if (rootNode.has("usageMetadata")) {
                JsonNode usage = rootNode.get("usageMetadata");
                int promptTokens = usage.has("promptTokenCount") ? usage.get("promptTokenCount").asInt() : 0;
                int candidateTokens = usage.has("candidatesTokenCount") ? usage.get("candidatesTokenCount").asInt() : 0;
                logger.info("[TOKEN USAGE] AI Analysis | Prompt: {} | Response: {} | Total: {} tokens",
                        promptTokens, candidateTokens, promptTokens + candidateTokens);
            }

            String aiResponse = rootNode.path("candidates").get(0).path("content").path("parts").get(0)
                    .path("text").asText();
            if (aiResponse != null) {
                aiResponse = aiResponse.trim();
                if (aiResponse.startsWith("```json")) {
                    aiResponse = aiResponse.substring(7);
                } else if (aiResponse.startsWith("```")) {
                    aiResponse = aiResponse.substring(3);
                }
                if (aiResponse.endsWith("```")) {
                    aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
                }
                aiResponse = aiResponse.trim();
            }
            return aiResponse;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Vertex AI response", e);
        }
    }

    // ---- AIAnalysis CRUD helpers (used by AIAnalysisController) ----

    public org.springframework.data.domain.Page<AIAnalysis> findAll(
            org.springframework.data.domain.Pageable pageable) {
        return aiAnalysisRepository.findAll(pageable);
    }

    public Optional<AIAnalysis> findById(Long id) {
        return aiAnalysisRepository.findById(id);
    }

    public AIAnalysis save(AIAnalysis analysis) {
        return aiAnalysisRepository.save(analysis);
    }

    public void deleteById(Long id) {
        aiAnalysisRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return aiAnalysisRepository.existsById(id);
    }

    /**
     * Sanitize LaTeX-style backslashes the model sometimes emits ({@code \frac}, {@code \left}…),
     * which are invalid JSON escapes. Escapes them so the JSON parses.
     */
    private String sanitizeJsonForLatex(String json) {
        if (json == null)
            return null;

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                if (next == '"' || next == '\\' || next == '/') {
                    result.append(c);
                    result.append(next);
                    i += 2;
                    continue;
                } else if (next == 'u' && i + 5 < json.length()) {
                    String hex = json.substring(i + 2, Math.min(i + 6, json.length()));
                    if (hex.matches("[0-9a-fA-F]{4}")) {
                        result.append(json.substring(i, i + 6));
                        i += 6;
                        continue;
                    }
                }
                if (next == 'n' || next == 'r' || next == 't' || next == 'b' || next == 'f') {
                    if (i + 2 < json.length() && Character.isLetter(json.charAt(i + 2))) {
                        result.append("\\\\");
                        i++;
                        continue;
                    } else {
                        result.append(c);
                        result.append(next);
                        i += 2;
                        continue;
                    }
                }
                if (Character.isLetter(next)) {
                    result.append("\\\\");
                    i++;
                    continue;
                }
                result.append("\\\\");
                result.append(next);
                i += 2;
            } else {
                result.append(c);
                i++;
            }
        }

        String sanitized = result.toString();
        if (!sanitized.equals(json)) {
            logger.info("Sanitized JSON: escaped LaTeX-style backslashes ({} chars added)",
                    (sanitized.length() - json.length()));
        }
        return sanitized;
    }

    /**
     * Escape raw control characters the model sometimes emits inside JSON string values (illegal
     * in JSON — must be escaped).
     */
    private String sanitizeControlCharacters(String json) {
        if (json == null)
            return null;

        StringBuilder result = new StringBuilder();
        boolean insideString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                result.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\' && insideString) {
                result.append(c);
                escaped = true;
                continue;
            }
            if (c == '"') {
                insideString = !insideString;
                result.append(c);
                continue;
            }
            if (insideString && c < 0x20) {
                switch (c) {
                    case '\n':
                        result.append("\\n");
                        break;
                    case '\r':
                        result.append("\\r");
                        break;
                    case '\t':
                        result.append("\\t");
                        break;
                    default:
                        result.append(" ");
                        break;
                }
            } else {
                result.append(c);
            }
        }

        String sanitized = result.toString();
        if (!sanitized.equals(json)) {
            logger.info("Sanitized JSON: escaped {} raw control characters",
                    (sanitized.length() - json.length()));
        }
        return sanitized;
    }
}
