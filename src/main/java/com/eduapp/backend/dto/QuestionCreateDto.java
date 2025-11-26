package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionType;
import java.util.List;

/**
 * DTO for creating or updating questions.
 * Used by admin to manage question content.
 */
public class QuestionCreateDto {
    private String text;
    private QuestionType type;
    private String correctAnswerText;
    private Integer marks;
    private List<QuestionOptionDto> options;

    public QuestionCreateDto() {
    }

    public QuestionCreateDto(String text, QuestionType type, String correctAnswerText,
            Integer marks, List<QuestionOptionDto> options) {
        this.text = text;
        this.type = type;
        this.correctAnswerText = correctAnswerText;
        this.marks = marks;
        this.options = options;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public List<QuestionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDto> options) {
        this.options = options;
    }
}
