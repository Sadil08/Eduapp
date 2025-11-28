package com.eduapp.backend.dto;

import java.time.LocalDateTime;

/**
 * Lightweight DTO for listing student paper attempts.
 * Used in the attempt history list view to show summary information without
 * heavy nested data.
 */
public class StudentPaperAttemptSummaryDto {
    private Long id;
    private Integer attemptNumber;
    private String status;
    private LocalDateTime completedAt;
    private Integer timeTakenMinutes;
    private Integer totalMarks;
    private String overallFeedbackSummary; // Truncated version for list view

    public StudentPaperAttemptSummaryDto() {
    }

    public StudentPaperAttemptSummaryDto(Long id, Integer attemptNumber, String status,
            LocalDateTime completedAt, Integer timeTakenMinutes,
            Integer totalMarks, String overallFeedbackSummary) {
        this.id = id;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.completedAt = completedAt;
        this.timeTakenMinutes = timeTakenMinutes;
        this.totalMarks = totalMarks;
        this.overallFeedbackSummary = overallFeedbackSummary;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTimeTakenMinutes() {
        return timeTakenMinutes;
    }

    public void setTimeTakenMinutes(Integer timeTakenMinutes) {
        this.timeTakenMinutes = timeTakenMinutes;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getOverallFeedbackSummary() {
        return overallFeedbackSummary;
    }

    public void setOverallFeedbackSummary(String overallFeedbackSummary) {
        this.overallFeedbackSummary = overallFeedbackSummary;
    }
}
