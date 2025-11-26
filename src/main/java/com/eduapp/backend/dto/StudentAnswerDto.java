package com.eduapp.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for student answers with AI analysis results.
 * Used to return answer details including marks and feedback after AI grading.
 */
public class StudentAnswerDto {
    private Long id;
    private Long attemptId;
    private Long questionId;
    private String questionText; // For display purposes
    private String answerText;
    private Long selectedOptionId;
    private LocalDateTime submittedAt;
    private Integer marksAwarded; // Marks given by AI
    private Integer marksAvailable; // Total marks for the question
    private String aiFeedback; // AI-generated feedback

    public StudentAnswerDto() {
    }

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
    public Long getId() {
        return id;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Integer getMarksAwarded() {
        return marksAwarded;
    }

    public Integer getMarksAvailable() {
        return marksAvailable;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setMarksAwarded(Integer marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public void setMarksAvailable(Integer marksAvailable) {
        this.marksAvailable = marksAvailable;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }
}
