package com.eduapp.backend.dto;

/**
 * One question + the student's answer for the TEACHER review screen. Includes the marking scheme
 * (correctAnswerText) and the AI's per-question mark/feedback so a teacher can decide an override.
 * Teacher/admin only — never returned to students.
 */
public record AttemptAnswerReviewDto(
        Long questionId,
        String questionText,
        Integer maxMarks,
        String markingScheme,
        String answerText,
        Integer aiMark,
        String aiFeedback) {
}
