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
    @org.springframework.transaction.annotation.Transactional
    public void analyzeAttempt(StudentPaperAttempt attempt) {
        logger.info("Starting AI analysis for attempt ID: {}", attempt.getId());

        // Re-fetch the attempt to ensure we have the latest state (especially answers)
        // and that we're working with an attached entity in this thread's context.
        StudentPaperAttempt freshAttempt = studentPaperAttemptRepository.findById(attempt.getId())
                .orElse(attempt);

        try {
            String prompt = buildPrompt(freshAttempt);
            String analysisResultJson = callGeminiApi(prompt);

            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(analysisResultJson);

            // Update Overall Analysis
            OverallPaperAnalysis analysis = new OverallPaperAnalysis();
            analysis.setAttempt(attempt);
            if (rootNode.has("overallFeedback")) {
                analysis.setOverallFeedback(rootNode.get("overallFeedback").asText());
            }
            // Update Student Answers with marks and feedback
            int totalObtainedMarks = 0;
            int totalAllocatedMarks = 0;

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
                        totalAllocatedMarks += (answer.getQuestion().getMarks() != null ? answer.getQuestion().getMarks() : 0);
                    }
                }
            }

            // Calculate Final Weighted Score
            Integer paperTotalMarks = attempt.getPaper().getTotalMarks();
            if (paperTotalMarks != null && totalAllocatedMarks > 0) {
                // Formula: (Obtained / Allocated) * PaperTotal
                double fraction = (double) totalObtainedMarks / totalAllocatedMarks;
                int weightedScore = (int) Math.round(fraction * paperTotalMarks);
                analysis.setTotalMarks(weightedScore);
            } else if (rootNode.has("totalMarks")) {
                // Fallback to AI provided marks if paper total marks not set
                analysis.setTotalMarks(rootNode.get("totalMarks").asInt());
            }

            analysisRepository.save(analysis);

            logger.info("AI analysis completed and saved for attempt ID: {}", attempt.getId());

        } catch (Exception e) {
            logger.error("Error during AI analysis for attempt ID: {}", attempt.getId(), e);
            
            // IMPORTANT: Update attempt with error state so frontend knows to show retry button
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
                        You are an experienced exam marker. Analyze the following student paper attempt and grade each question.

                        **CRITICAL MARKING INSTRUCTIONS:**

                        1. **For MCQ Questions**: Award full marks if the selected option is correct, 0 marks otherwise.

                        2. **For ESSAY/SHORT_ANSWER Questions with a Marking Scheme**:
                           - The 'Correct Answer' field contains a MARKING SCHEME with specific criteria and marks.
                           - You MUST follow this scheme EXACTLY:
                             - Award marks ONLY for the specific steps/criteria shown in the scheme.
                             - Each criterion has a mark allocation (e.g., B1, M1, A1 = 1 mark each; DM1 = dependent 1 mark).
                             - Check if the student's answer contains each required step.
                             - Sum the marks for criteria the student satisfies.
                             - Do NOT award full marks just because the final answer is correct - each step must be shown.
                           - If the scheme has a table format (Answer | Marks | Guidance), follow it strictly.

                        3. **For ESSAY Questions without a structured scheme**:
                           - Break down the question into logical marking points.
                           - Award partial marks for partially correct answers.
                           - Be strict but fair.

                        4. **For UNANSWERED Questions** (where Student's Answer is "No answer provided"):
                           - Award 0 marks.
                           - Provide feedback: "Question not attempted."

                        **OUTPUT FORMAT:**
                        Respond ONLY with valid JSON (no markdown code blocks). The JSON must have:
                        - 'questions': Array of {questionId, marksAwarded, feedback}
                          - marksAwarded: integer between 0 and the maximum marks for that question
                          - feedback: detailed explanation of marks awarded/deducted referencing the marking scheme.
                            **IMPORTANT: Write feedback in PLAIN TEXT only. Do NOT use LaTeX syntax.
                            Instead of LaTeX like backslash-frac, backslash-cosh, use readable text like "3/4", "cosh(1)", "x^2", etc.**
                        - 'overallFeedback': string with summary and improvement suggestions (in plain text, no LaTeX)
                        - 'totalMarks': integer (sum of all marksAwarded)

                        **PAPER TO MARK:**

                        """);
        sb.append("Paper: ").append(attempt.getPaper().getName()).append("\n");
        sb.append("Description: ").append(attempt.getPaper().getDescription()).append("\n\n");

        for (StudentAnswer answer : attempt.getAnswers()) {
            sb.append("---\n");
            sb.append("Question ID: ").append(answer.getQuestion().getId()).append("\n");
            sb.append("Question Type: ").append(answer.getQuestion().getType()).append("\n");
            String qText = (answer.getQuestion().getExtractedText() != null
                    && !answer.getQuestion().getExtractedText().isEmpty())
                            ? answer.getQuestion().getExtractedText()
                            : answer.getQuestion().getText();
            sb.append("Question: ").append(qText).append("\n");
            sb.append("Maximum Marks: ").append(answer.getQuestion().getMarks() != null ? answer.getQuestion().getMarks() : "N/A").append("\n");
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