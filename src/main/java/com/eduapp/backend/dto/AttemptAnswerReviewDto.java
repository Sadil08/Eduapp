package com.eduapp.backend.dto;

import com.eduapp.backend.media.SignedUrlSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * One question + the student's answer for the TEACHER review screen. Includes the marking scheme
 * (correctAnswerText) and the AI's per-question mark/feedback so a teacher can decide an override.
 * Teacher/admin only — never returned to students. {@code imageUrl} (handwritten upload) is
 * serialized as a short-lived signed URL.
 */
public record AttemptAnswerReviewDto(
        Long questionId,
        String questionText,
        Integer maxMarks,
        String markingScheme,
        String answerText,
        @JsonSerialize(using = SignedUrlSerializer.class) String imageUrl,
        Integer aiMark,
        String aiFeedback) {
}
