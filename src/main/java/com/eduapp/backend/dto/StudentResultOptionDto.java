package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionOption;

/**
 * An MCQ option as shown on a student's RELEASED result. Unlike the attempt-time option, this
 * one DOES reveal {@code correct} — but only ever reached this DTO after the teacher released
 * results — plus whether the student {@code selected} it.
 */
public record StudentResultOptionDto(Long id, String keyLabel, String text, boolean correct, boolean selected) {

    public static StudentResultOptionDto from(QuestionOption o, Long selectedOptionId) {
        return new StudentResultOptionDto(o.getId(), o.getKeyLabel(), o.getText(),
                Boolean.TRUE.equals(o.getIsCorrect()), o.getId().equals(selectedOptionId));
    }
}
