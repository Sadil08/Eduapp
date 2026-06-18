package com.eduapp.backend.dto;

import com.eduapp.backend.model.Question;

/**
 * A school-paper question as shown to a STUDENT while attempting — deliberately OMITS
 * {@code correctAnswerText} (the marking scheme) so the answer is never exposed.
 */
public record StudentQuestionDto(Long id, String text, String type, Integer marks) {

    public static StudentQuestionDto from(Question q) {
        return new StudentQuestionDto(q.getId(), q.getText(),
                q.getType() != null ? q.getType().name() : null, q.getMarks());
    }
}
