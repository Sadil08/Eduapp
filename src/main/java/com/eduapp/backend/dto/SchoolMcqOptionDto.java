package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionOption;

/** Teacher-facing MCQ option — includes {@code correct} (the answer key). Never sent to students. */
public record SchoolMcqOptionDto(Long id, String keyLabel, String text, boolean correct, Integer orderIndex) {

    public static SchoolMcqOptionDto from(QuestionOption o) {
        return new SchoolMcqOptionDto(o.getId(), o.getKeyLabel(), o.getText(),
                Boolean.TRUE.equals(o.getIsCorrect()), o.getOrderIndex());
    }
}
