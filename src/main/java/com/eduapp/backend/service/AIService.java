package com.eduapp.backend.service;

import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.StudentAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
@SuppressWarnings("null")
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String AI_SERVICE_URL = "http://localhost:8000";

    public String extractTextFromImage(MultipartFile file) {
        return extractTextFromImage(file, null, null);
    }

    public String extractTextFromImage(MultipartFile file, String subjectName) {
        return extractTextFromImage(file, subjectName, null);
    }

    public String extractTextFromImage(MultipartFile file, String subjectName, String lessonName) {
        try {
            logger.info("Sending image to AI service for extraction (subject: {}, lesson: {})", subjectName,
                    lessonName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            // Add subject parameter if provided
            if (subjectName != null && !subjectName.isEmpty()) {
                body.add("subject", subjectName);
            }

            // Add lesson parameter if provided
            if (lessonName != null && !lessonName.isEmpty()) {
                body.add("lesson", lessonName);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    AI_SERVICE_URL + "/extract",
                    requestEntity,
                    Map.class);

            if (response.getBody() != null && response.getBody().containsKey("extracted_text")) {
                return (String) response.getBody().get("extracted_text");
            }
            return "";
        } catch (Exception e) {
            logger.error("Failed to extract text from image: {}", e.getMessage());
            return ""; // Fallback or throw
        }
    }

    /**
     * Batch extract text from multiple images in a single API call.
     * Reduces API overhead for admin operations like creating papers with multiple
     * questions.
     * 
     * @param filesWithIds Map of identifier to file (e.g., questionId -> image
     *                     file)
     * @param subjectName  Optional subject for specialized extraction
     * @return Map of identifier to extracted text
     */
    public Map<String, String> extractTextFromImagesBatch(
            Map<String, MultipartFile> filesWithIds,
            String subjectName) {

        if (filesWithIds == null || filesWithIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            logger.info("Sending {} images to AI service for batch extraction (subject: {})",
                    filesWithIds.size(), subjectName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Collect IDs in order
            List<String> idList = new ArrayList<>();

            for (Map.Entry<String, MultipartFile> entry : filesWithIds.entrySet()) {
                String id = entry.getKey();
                MultipartFile file = entry.getValue();
                idList.add(id);

                body.add("files", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            }

            // Add comma-separated IDs
            body.add("ids", String.join(",", idList));

            // Add subject parameter if provided
            if (subjectName != null && !subjectName.isEmpty()) {
                body.add("subject", subjectName);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    AI_SERVICE_URL + "/extract-batch",
                    requestEntity,
                    Map.class);

            if (response.getBody() != null && response.getBody().containsKey("results")) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> results = (List<Map<String, String>>) response.getBody().get("results");

                Map<String, String> extractedTexts = new java.util.HashMap<>();
                for (Map<String, String> result : results) {
                    extractedTexts.put(result.get("id"), result.get("extracted_text"));
                }

                logger.info("Batch extraction completed: {} images processed", extractedTexts.size());
                return extractedTexts;
            }

            return Collections.emptyMap();
        } catch (Exception e) {
            logger.error("Failed to batch extract text from images: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private final QuestionModelAnswerService modelAnswerService;

    public AIService(QuestionModelAnswerService modelAnswerService) {
        this.modelAnswerService = modelAnswerService;
    }

    public Map<String, Object> analyzeAnswer(StudentAnswer answer) {
        if (answer == null) {
            throw new IllegalArgumentException("StudentAnswer cannot be null");
        }
        logger.info("Analyzing answer for question: {}", answer.getQuestion().getId());

        Question question = answer.getQuestion();

        // Get question text (prefer extracted if available)
        String questionText = question.getExtractedText() != null && !question.getExtractedText().isEmpty()
                ? question.getExtractedText()
                : question.getText();

        // Get model answer text from QuestionModelAnswer entity
        String modelAnswerText = modelAnswerService.getModelAnswerTextForAnalysis(question);

        // Get student answer text (prefer extracted from image, fallback to typed)
        String studentAnswerText = "";
        if (answer.getExtractedText() != null && !answer.getExtractedText().isEmpty()) {
            studentAnswerText = answer.getExtractedText();
        } else if (answer.getAnswerText() != null) {
            studentAnswerText = answer.getAnswerText();
        } else if (answer.getSelectedOption() != null) {
            studentAnswerText = answer.getSelectedOption().getText();
        }

        // Prepare payload for FastAPI /mark
        Map<String, Object> payload = Map.of(
                "question_text", questionText,
                "model_answer_text", modelAnswerText != null ? modelAnswerText : "",
                "student_answer_text", studentAnswerText,
                "total_marks", question.getMarks() != null ? question.getMarks() : 10);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    AI_SERVICE_URL + "/mark",
                    payload,
                    Map.class);

            if (response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return Map.of(
                        "feedback", body.getOrDefault("feedback", "No feedback"),
                        "marks", body.getOrDefault("marks_awarded", 0),
                        "lessonsToReview", body.getOrDefault("lessons_to_review", ""));
            }
        } catch (Exception e) {
            logger.error("AI call failed: {}", e.getMessage(), e);
        }

        return Map.of("feedback", "Analysis unavailable", "marks", 0, "lessonsToReview", "");
    }
}