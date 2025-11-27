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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class AIAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);

    @Value("${gemini.api-key:YOUR_GEMINI_API_KEY}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1}")
    private String apiUrl;

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
    public void analyzeAttempt(StudentPaperAttempt attempt) {
        logger.info("Starting AI analysis for attempt ID: {}", attempt.getId());

        try {
            String prompt = buildPrompt(attempt);
            String analysisResultJson = callGeminiApi(prompt);

            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(analysisResultJson);

            // Update Overall Analysis
            OverallPaperAnalysis analysis = new OverallPaperAnalysis();
            analysis.setAttempt(attempt);
            if (rootNode.has("overallFeedback")) {
                analysis.setOverallFeedback(rootNode.get("overallFeedback").asText());
            }
            if (rootNode.has("totalMarks")) {
                analysis.setTotalMarks(rootNode.get("totalMarks").asInt());
            }
            analysisRepository.save(analysis);

            // Update Student Answers with marks and feedback
            if (rootNode.has("questions")) {
                for (JsonNode qNode : rootNode.get("questions")) {
                    Long qId = qNode.get("questionId").asLong();
                    Integer marks = qNode.get("marksAwarded").asInt();
                    String feedback = qNode.get("feedback").asText();

                    // Find and update the answer
                    attempt.getAnswers().stream()
                            .filter(a -> a.getQuestion().getId().equals(qId))
                            .findFirst()
                            .ifPresent(answer -> {
                                answer.setMarksAwarded(marks);
                                answer.setAiFeedback(feedback);
                                // Save the updated answer explicitly
                                studentAnswerRepository.save(answer);
                            });
                }
            }

            logger.info("AI analysis completed and saved for attempt ID: {}", attempt.getId());

        } catch (Exception e) {
            logger.error("Error during AI analysis for attempt ID: {}", attempt.getId(), e);
        }
    }

    private String buildPrompt(StudentPaperAttempt attempt) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "Analyze the following student paper attempt. Provide output in strict JSON format. " +
                        "The JSON should be an object with a 'questions' array. Each item in the array should have: " +
                        "'questionId' (integer), 'marksAwarded' (integer), and 'feedback' (string). " +
                        "Also include a top-level 'overallFeedback' (string) and 'totalMarks' (integer). " +
                        "For MCQs, give full marks if correct, 0 if incorrect. For others, grade based on the answer.\n\n");
        sb.append("Paper: ").append(attempt.getPaper().getName()).append("\n");
        sb.append("Description: ").append(attempt.getPaper().getDescription()).append("\n\n");

        for (StudentAnswer answer : attempt.getAnswers()) {
            sb.append("Question ID: ").append(answer.getQuestion().getId()).append("\n");
            sb.append("Question: ").append(answer.getQuestion().getText()).append("\n");
            sb.append("Marks Available: ").append(answer.getQuestion().getMarks()).append("\n");
            sb.append("Correct Answer: ").append(answer.getQuestion().getCorrectAnswerText()).append("\n");
            sb.append("Student Answer: ").append(answer.getAnswerText());
            if (answer.getSelectedOption() != null) {
                sb.append(" (Option: ").append(answer.getSelectedOption().getText()).append(")");
            }
            sb.append("\n\n");
        }

        return sb.toString();
    }

    private String callGeminiApi(String prompt) throws Exception {
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY")) {
            logger.warn("Gemini API key is not configured. Skipping actual API call.");
            throw new IllegalStateException("AI Analysis failed: API Key not configured.");
        }

        String url = String.format("%s/models/%s:generateContent?key=%s", apiUrl, model, apiKey);
        logger.info("Calling Gemini API with model: {}", model);

        // Construct Request Body
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Gemini API call successful");
            JsonNode root = objectMapper.readTree(response.getBody());
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
            logger.error("Gemini API call failed with status: {}", response.getStatusCode());
            throw new RuntimeException("Gemini API call failed with status: " + response.getStatusCode());
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
}