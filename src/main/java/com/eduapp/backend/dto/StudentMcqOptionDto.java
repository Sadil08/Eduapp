package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionOption;

/**
 * Student-facing MCQ option while attempting. Deliberately OMITS {@code isCorrect} — the answer
 * key must never reach a student.
 */
public record StudentMcqOptionDto(Long id, String keyLabel, String text, Integer orderIndex) {

    public static StudentMcqOptionDto from(QuestionOption o) {
        return new StudentMcqOptionDto(o.getId(), o.getKeyLabel(), o.getText(), o.getOrderIndex());
    }
}
