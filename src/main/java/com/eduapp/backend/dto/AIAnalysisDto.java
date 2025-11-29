package com.eduapp.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for AI analysis feedback on student answers.
 */
public class AIAnalysisDto {
    private Long id;
    private Long answerId;
    private String feedback;
    private Integer marks;
    private String lessonsToReview;
    private LocalDateTime createdAt;

    public AIAnalysisDto() {}

    public AIAnalysisDto(Long id, Long answerId, String feedback, Integer marks, String lessonsToReview, LocalDateTime createdAt) {
        this.id = id;
        this.answerId = answerId;
        this.feedback = feedback;
        this.marks = marks;
        this.lessonsToReview = lessonsToReview;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getLessonsToReview() { return lessonsToReview; }
    public void setLessonsToReview(String lessonsToReview) { this.lessonsToReview = lessonsToReview; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}