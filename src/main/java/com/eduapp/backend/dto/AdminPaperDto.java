package com.eduapp.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin paper management.
 * Extends PaperDetailDto with additional statistics and metadata.
 */
public class AdminPaperDto extends PaperDetailDto {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private int totalAttempts;
    private Double averageScore;

    public AdminPaperDto() {
        super();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
}
