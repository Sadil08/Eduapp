package com.eduapp.backend.dto;

import java.time.LocalDateTime;

public class StudentAnswerDto {
    private Long id;
    private Long attemptId;
    private Long questionId;
    private String answerText;
    private Long selectedOptionId;
    private LocalDateTime submittedAt;

    public StudentAnswerDto() {}

    public StudentAnswerDto(Long id, Long attemptId, Long questionId, String answerText,
                            Long selectedOptionId, LocalDateTime submittedAt) {
        this.id = id;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.answerText = answerText;
        this.selectedOptionId = selectedOptionId;
        this.submittedAt = submittedAt;
    }

    // Getters
    public Long getId() { return id; }
    public Long getAttemptId() { return attemptId; }
    public Long getQuestionId() { return questionId; }
    public String getAnswerText() { return answerText; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
