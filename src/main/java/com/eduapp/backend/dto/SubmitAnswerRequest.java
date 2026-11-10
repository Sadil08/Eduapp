package com.eduapp.backend.dto;

import jakarta.validation.constraints.NotNull;

public class SubmitAnswerRequest {
    @NotNull(message = "questionId is required")
    private Long questionId;
    private String answerText;
    private Long selectedOptionId; // set for MCQ questions instead of answerText

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
}
