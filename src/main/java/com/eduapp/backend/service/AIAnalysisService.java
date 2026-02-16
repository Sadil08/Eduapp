package com.eduapp.backend.service;

import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.model.OverallPaperAnalysis;
import com.eduapp.backend.model.AIAnalysis;
import com.eduapp.backend.repository.OverallPaperAnalysisRepository;
import com.eduapp.backend.repository.AIAnalysisRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import com.eduapp.backend.repository.StudentAnswerRepository;
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
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

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

    private final OverallPaperAnalysisRepository analysisRepository;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final StudentPaperAttemptRepository studentPaperAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIAnalysisService(OverallPaperAnalysisRepository analysisRepository,
            AIAnalysisRepository aiAnalysisRepository,
            StudentPaperAttemptRepository studentPaperAttemptRepository,
            StudentAnswerRepository studentAnswerRepository) {
        this.analysisRepository = analysisRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.studentPaperAttemptRepository = studentPaperAttemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Async
    @org.springframework.transaction.annotation.Transactional
    public void analyzeAttempt(StudentPaperAttempt attempt) {
        logger.info("Starting AI analysis for attempt ID: {}", attempt.getId());

        // Re-fetch the attempt to ensure we have the latest state (especially answers)
        // and that we're working // IMPORTANT: Use findByIdWithAnswers to eagerly load
        // all answers
        // This ensures the AI analysis sees ALL questions, including for custom bundles
        StudentPaperAttempt freshAttempt = studentPaperAttemptRepository.findByIdWithAnswers(attempt.getId())
                .orElse(attempt);

        logger.info("AI analysis: freshAttempt has {} answers loaded", freshAttempt.getAnswers().size());

        try {
            String prompt = buildPrompt(freshAttempt);
            String analysisResultJson = callGeminiApi(prompt);

            // Sanitize the JSON response to handle LaTeX backslashes
            // The AI sometimes uses LaTeX like \frac, \left, etc. which break JSON parsing
            // because \f, \l, etc. are invalid escape sequences
            analysisResultJson = sanitizeJsonForLatex(analysisResultJson);

            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(analysisResultJson);

            // Update Overall Analysis
            OverallPaperAnalysis analysis = new OverallPaperAnalysis();
            analysis.setAttempt(freshAttempt);
            if (rootNode.has("overallFeedback")) {
                analysis.setOverallFeedback(rootNode.get("overallFeedback").asText());
            }
            // Update Student Answers with marks and feedback
            int totalObtainedMarks = 0;

            // IMPORTANT: Calculate total allocated marks from ALL questions in the attempt,
            // not just the ones the AI returns. This ensures correct percentage
            // calculation.
            int totalAllocatedMarks = freshAttempt.getAnswers().stream()
                    .mapToInt(a -> a.getQuestion().getMarks() != null ? a.getQuestion().getMarks() : 0)
                    .sum();

            if (rootNode.has("questions")) {
                for (JsonNode qNode : rootNode.get("questions")) {
                    Long qId = qNode.get("questionId").asLong();
                    Integer marks = qNode.get("marksAwarded").asInt();
                    String feedback = qNode.get("feedback").asText();

                    // Find and update the answer
                    Optional<StudentAnswer> answerOpt = freshAttempt.getAnswers().stream()
                            .filter(a -> a.getQuestion().getId().equals(qId))
                            .findFirst();

                    if (answerOpt.isPresent()) {
                        StudentAnswer answer = answerOpt.get();
                        answer.setMarksAwarded(marks);
                        answer.setAiFeedback(feedback);
                        // Save the updated answer explicitly
                        studentAnswerRepository.save(answer);

                        totalObtainedMarks += marks;
                    }
                }
            }

            // Calculate Final Weighted Score using correct totals
            Integer paperTotalMarks = freshAttempt.getPaper().getTotalMarks();
            logger.info("Marks calculation: obtained={}, allocated={}, paperTotal={}",
                    totalObtainedMarks, totalAllocatedMarks, paperTotalMarks);

            if (paperTotalMarks != null && totalAllocatedMarks > 0) {
                // Formula: (Obtained / Allocated) * PaperTotal
                double fraction = (double) totalObtainedMarks / totalAllocatedMarks;
                int weightedScore = (int) Math.round(fraction * paperTotalMarks);
                analysis.setTotalMarks(weightedScore);
                logger.info("Weighted score: {} (fraction: {})", weightedScore, fraction);
            } else if (rootNode.has("totalMarks")) {
                // Fallback to AI provided marks if paper total marks not set
                analysis.setTotalMarks(rootNode.get("totalMarks").asInt());
            }

            analysisRepository.save(analysis);

            logger.info("AI analysis completed and saved for attempt ID: {}", attempt.getId());

        } catch (Exception e) {
            logger.error("Error during AI analysis for attempt ID: {}", attempt.getId(), e);

            // IMPORTANT: Update attempt with error state so frontend knows to show retry
            // button
            try {
                // Fetch fresh to avoid detached entity issues
                StudentPaperAttempt errorAttempt = studentPaperAttemptRepository.findById(attempt.getId())
                        .orElse(attempt);

                String errorMessage = e.getMessage();
                if (e instanceof org.springframework.web.client.HttpClientErrorException.TooManyRequests) {
                    errorMessage = "AI Service busy (Rate Limit Exceeded). Please retry in a few moments.";
                }

                errorAttempt.setAnalysisError(errorMessage);
                errorAttempt.setAnalysisAttempted(true);
                errorAttempt.setAnalysisCompleted(false);
                studentPaperAttemptRepository.save(errorAttempt);
                logger.info("Updated attempt {} with analysis error", attempt.getId());
            } catch (Exception dbError) {
                logger.error("Failed to save analysis error state for attempt {}", attempt.getId(), dbError);
            }
        }
    }

    private String buildPrompt(StudentPaperAttempt attempt) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                """
                        You are an experienced, supportive TEACHER and exam marker. Your role is not just to grade answers, but to EDUCATE the student by explaining their mistakes and helping them learn.

                        **YOUR DUAL ROLE:**
                        1. **As a Marker**: Grade each answer fairly according to the marking scheme.
                        2. **As a Teacher**: Explain WHERE the student went wrong and WHY, and guide them to the correct understanding.

                        **CRITICAL MARKING INSTRUCTIONS:**

                        1. **For MCQ Questions**:
                           - Award full marks if the selected option is correct, 0 marks otherwise.
                           - If incorrect, explain WHY their choice was wrong and WHY the correct answer is right.
                           - Provide the reasoning/concept behind the correct answer.

                        2. **For ESSAY/SHORT_ANSWER Questions with a Marking Scheme**:
                           - The 'Correct Answer' field contains a MARKING SCHEME with specific criteria and marks.
                           - You MUST follow this scheme EXACTLY:
                             - Award marks ONLY for the specific steps/criteria shown in the scheme.
                             - Each criterion has a mark allocation (e.g., B1, M1, A1 = 1 mark each; DM1 = dependent 1 mark).
                             - Check if the student's answer contains each required step.
                             - Sum the marks for criteria the student satisfies.
                             - Do NOT award full marks just because the final answer is correct - each step must be shown.
                           - If the scheme has a table format (Answer | Marks | Guidance), follow it strictly.
                           - **TEACHER FEEDBACK**: For each mark lost, explain:
                             - What specific step/criteria was missing or incorrect
                             - What the student should have written instead
                             - The concept or principle they need to understand

                        3. **For Questions WITHOUT a Marking Scheme** (Correct Answer is empty/null/generic):
                           - Create your OWN logical marking breakdown based on the maximum marks.
                           - For example, if a question is worth 5 marks with no scheme, divide it into 5 marking points.
                           - Award marks for: correct method, correct working, correct answer, clear presentation.
                           - Be fair and consistent in your breakdown.
                           - **TEACHER FEEDBACK**: Explain how you allocated the marks and what was missing.

                        4. **For UNANSWERED Questions** (where Student's Answer is "No answer provided"):
                           - Award 0 marks.
                           - **IMPORTANT - TEACH THE SOLUTION**: Instead of just saying "not attempted", provide:
                             - A step-by-step explanation of HOW to solve this question
                             - Use the marking scheme as your guide for the steps
                             - Show the working/reasoning for each step
                             - Explain the key concepts needed to answer correctly
                             - This helps the student learn even from questions they skipped

                        5. **CRITICAL: Identifying the FINAL ANSWER from Handwritten/Extracted Content**:
                           - Student work may be extracted from handwritten images via OCR.
                           - **IGNORE any text that appears to be crossed-out, struck-through, or deleted** (often appears at the start or randomly in the text as stray values).
                           - The FINAL ANSWER is typically:
                             * The last clearly stated result in the working (e.g., "= 4" at the end)
                             * Text that is underlined, double-underlined, circled, or boxed
                             * Explicitly labeled as "Answer = " or "Final answer:"
                           - **If the working shows correct steps leading to a correct result, but there's a stray/crossed-out value elsewhere, TRUST THE WORKING**.
                           - Award marks based on the logical flow of the solution, not random numbers that may appear at the start of extracted text.
                           - When in doubt, follow the mathematical working to determine what the student's intended answer is.

                        **OUTPUT FORMAT:**
                        Respond ONLY with valid JSON (no markdown code blocks). The JSON must have:

                        **CRITICAL: You MUST return an entry for EVERY question in the paper. Do NOT skip any questions.**

                        - 'questions': Array of {questionId, marksAwarded, feedback}
                          - **MUST include ALL questions from the paper - answered, partially answered, AND unanswered**
                          - marksAwarded: integer between 0 and the maximum marks for that question
                          - feedback: DETAILED teacher-like explanation that includes:
                            * Marks breakdown referencing the marking scheme
                            * For correct answers: Acknowledge the good work and reinforce the concepts
                            * For incorrect/partial answers: Clear explanation of mistakes, what was missing, and the correct approach
                            * **For unanswered questions (MANDATORY): Provide a COMPLETE step-by-step solution showing HOW to solve the question using the marking scheme. This is educational - teach the student as if you are their tutor.**
                            * Even for 0-mark answers, provide educational value
                            * Encouraging but honest tone
                            **IMPORTANT: Write feedback in PLAIN TEXT only. Do NOT use LaTeX syntax.
                            Instead of LaTeX like backslash-frac, backslash-cosh, use readable text like "3/4", "cosh(1)", "x^2", etc.**
                        - 'overallFeedback': string with:
                          * Summary of performance (mention how many questions were attempted vs total)
                          * Key areas where the student needs to improve
                          * Specific study recommendations
                          * Encouraging closing message
                          (in plain text, no LaTeX)
                        - 'totalMarks': integer (sum of all marksAwarded across ALL questions)

                        **PAPER TO MARK:**

                        """);

        // Add explicit question count so AI knows how many to return
        int questionCount = attempt.getAnswers().size();
        sb.append("Paper: ").append(attempt.getPaper().getName()).append("\n");
        sb.append("Description: ").append(attempt.getPaper().getDescription()).append("\n");
        sb.append("\n**IMPORTANT: This paper contains exactly ").append(questionCount)
                .append(" questions. You MUST return feedback for all ").append(questionCount)
                .append(" questions.**\n\n");

        logger.info("Building AI prompt for attempt {} with {} questions", attempt.getId(), questionCount);

        for (StudentAnswer answer : attempt.getAnswers()) {
            sb.append("---\n");
            sb.append("Question ID: ").append(answer.getQuestion().getId()).append("\n");
            sb.append("Question Type: ").append(answer.getQuestion().getType()).append("\n");
            String qText = (answer.getQuestion().getExtractedText() != null
                    && !answer.getQuestion().getExtractedText().isEmpty())
                            ? answer.getQuestion().getExtractedText()
                            : answer.getQuestion().getText();
            sb.append("Question: ").append(qText).append("\n");
            sb.append("Maximum Marks: ")
                    .append(answer.getQuestion().getMarks() != null ? answer.getQuestion().getMarks() : "N/A")
                    .append("\n");
            sb.append("Correct Answer / Marking Scheme:\n").append(answer.getQuestion().getCorrectAnswerText())
                    .append("\n\n");

            // Format student answer properly for MCQs vs text questions
            sb.append("Student's Answer: ");
            if (answer.getSelectedOption() != null) {
                // For MCQ: show option text as the answer, with option ID for clarity
                sb.append(answer.getSelectedOption().getText());
                sb.append(" (Selected Option ID: ").append(answer.getSelectedOption().getId()).append(")");
            } else {
                // For text/essay questions: show the answer text (prefer extracted if
                // available)
                String studentAns = (answer.getExtractedText() != null && !answer.getExtractedText().isEmpty())
                        ? answer.getExtractedText()
                        : answer.getAnswerText();

                logger.warn("AI ANALYSIS DEBUG: questionId={}, studentAns='{}' (extracted={}, typed={})",
                        answer.getQuestion().getId(),
                        (studentAns != null && !studentAns.isEmpty()) ? studentAns : "EMPTY",
                        answer.getExtractedText() != null && !answer.getExtractedText().isEmpty(),
                        answer.getAnswerText() != null && !answer.getAnswerText().isEmpty());

                sb.append(studentAns != null && !studentAns.isEmpty() ? studentAns : "No answer provided");
            }
            sb.append("\n\n");
        }

        return sb.toString();
    }

    /**
     * Get an OAuth2 access token from the service account credentials.
     * Vertex AI requires Bearer token auth instead of API key.
     */
    private String getAccessToken() throws IOException {
        GoogleCredentials credentials;
        if (gcpCredentialsPath != null && !gcpCredentialsPath.isEmpty()
                && !gcpCredentialsPath.equals("/path/to/your/service-account-key.json")) {
            credentials = GoogleCredentials.fromStream(new FileInputStream(gcpCredentialsPath))
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        } else {
            // Fall back to Application Default Credentials (ADC)
            // Works when running on GCP or when GOOGLE_APPLICATION_CREDENTIALS env var is
            // set
            credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        }
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    private String callGeminiApi(String prompt) throws Exception {
        if (gcpProjectId == null || gcpProjectId.isEmpty() || gcpProjectId.equals("YOUR_GCP_PROJECT_ID")) {
            logger.warn("GCP Project ID is not configured. Skipping actual API call.");
            throw new IllegalStateException("AI Analysis failed: GCP Project ID not configured.");
        }

        // Vertex AI endpoint format
        String url = String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                gcpLocation, gcpProjectId, gcpLocation, model);
        logger.info("Calling Vertex AI with model: {} in {}", model, gcpLocation);

        // Construct Request Body (same format as AI Studio)
        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        // Use OAuth2 Bearer token instead of API key
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessToken());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Vertex AI call successful");
            JsonNode root = objectMapper.readTree(response.getBody());

            // Log token usage for cost tracking
            if (root.has("usageMetadata")) {
                JsonNode usage = root.get("usageMetadata");
                int promptTokens = usage.has("promptTokenCount") ? usage.get("promptTokenCount").asInt() : 0;
                int candidateTokens = usage.has("candidatesTokenCount") ? usage.get("candidatesTokenCount").asInt() : 0;
                int totalTokens = promptTokens + candidateTokens;
                logger.info("[TOKEN USAGE] AI Analysis | Prompt: {} | Response: {} | Total: {} tokens",
                        promptTokens, candidateTokens, totalTokens);
            }

            String aiResponse = root.path("candidates").get(0).path("content").path("parts").get(0).path("text")
                    .asText();
            logger.debug("Raw AI Response: {}", aiResponse);

            // Clean the response if it contains markdown code blocks
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
            logger.debug("Cleaned AI Response: {}", aiResponse);
            return aiResponse;
        } else {
            logger.error("Vertex AI call failed with status: {}", response.getStatusCode());
            throw new RuntimeException("Vertex AI call failed with status: " + response.getStatusCode());
        }
    }

    // CRUD Methods for AIAnalysis (Legacy/Existing Support)
    public List<AIAnalysis> findAll() {
        return aiAnalysisRepository.findAll();
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
     * Sanitize JSON response to handle LaTeX backslashes.
     * The AI sometimes returns LaTeX notation like \frac, \left, \cos, etc.
     * These are invalid JSON escape sequences.
     * This method escapes backslashes that are part of LaTeX commands.
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

                // Check for definitely valid JSON escapes (standalone, not followed by more
                // letters)
                if (next == '"' || next == '\\' || next == '/') {
                    // These are always valid JSON escapes
                    result.append(c);
                    result.append(next);
                    i += 2;
                    continue;
                } else if (next == 'u' && i + 5 < json.length()) {
                    // Unicode escape (backslash-u followed by 4 hex digits) - check if valid
                    String hex = json.substring(i + 2, Math.min(i + 6, json.length()));
                    if (hex.matches("[0-9a-fA-F]{4}")) {
                        result.append(json.substring(i, i + 6));
                        i += 6;
                        continue;
                    }
                }

                // For n, r, t, b, f - these COULD be valid JSON escapes OR LaTeX commands
                // If followed by more letters, it's likely LaTeX (e.g., \frac, \nabla, \begin)
                if (next == 'n' || next == 'r' || next == 't' || next == 'b' || next == 'f') {
                    // Check if followed by more letters (suggests LaTeX command)
                    if (i + 2 < json.length() && Character.isLetter(json.charAt(i + 2))) {
                        // Likely LaTeX like \frac, \nabla, \rightarrow, \begin, \text
                        result.append("\\\\");
                        i++;
                        continue;
                    } else {
                        // Just a single-char escape like \n, \r, \t, \b, \f
                        result.append(c);
                        result.append(next);
                        i += 2;
                        continue;
                    }
                }

                // Any other letter after backslash - definitely needs escaping
                if (Character.isLetter(next)) {
                    result.append("\\\\");
                    i++;
                    continue;
                }

                // Non-letter after backslash - keep as is
                result.append(c);
                i++;
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
}