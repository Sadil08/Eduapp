package com.eduapp.backend.service;

import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.StudentAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@SuppressWarnings("null")
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    // Placeholder AI endpoint
    private static final String AI_URL = "https://api.openai.com/v1/chat/completions"; // Example

    public Map<String, Object> analyzeAnswer(StudentAnswer answer) {
        if (answer == null) {
            throw new IllegalArgumentException("StudentAnswer cannot be null");
        }
        logger.info("Analyzing answer for question: {}", answer.getQuestion().getId());

        // Prepare payload
        Map<String, Object> payload = Map.of(
            "question", answer.getQuestion().getText(),
            "correctAnswer", answer.getQuestion().getCorrectAnswerText(),
            "studentAnswer", answer.getAnswerText() != null ? answer.getAnswerText() : (answer.getSelectedOption() != null ? answer.getSelectedOption().getText() : "")
        );

        try {
            // Call AI (placeholder)
            // Map<String, Object> response = restTemplate.postForObject(AI_URL, payload, Map.class);
            // For now, mock response
            Map<String, Object> mockResponse = Map.of(
                "feedback", "Good attempt, but review the concept.",
                "marks", 5,
                "lessonsToReview", "Basic algebra"
            );
            logger.info("AI analysis completed");
            return mockResponse;
        } catch (Exception e) {
            logger.error("AI call failed: {}", e.getMessage(), e);
            return Map.of("feedback", "Analysis unavailable", "marks", 0, "lessonsToReview", "");
        }
    }
}