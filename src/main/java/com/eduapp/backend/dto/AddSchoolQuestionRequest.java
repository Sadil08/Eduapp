package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionType;
import jakarta.validation.constraints.NotBlank;

public class AddSchoolQuestionRequest {
    @NotBlank(message = "Question text is required")
    private String text;
    private QuestionType type;
    private Integer marks;
    private String correctAnswerText;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }
    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }
}
