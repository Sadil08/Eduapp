package com.eduapp.backend.dto;

import com.eduapp.backend.model.ProgressStatus;
import java.time.LocalDateTime;

/**
 * DTO for user progress on bundles and papers.
 */
public class ProgressDto {
    private Long id;
    private Long userId;
    private Long bundleId;
    private Long paperId;
    private ProgressStatus status;
    private Float completionPercentage;
    private Integer timeSpentMinutes;
    private LocalDateTime updatedAt;

    public ProgressDto() {}

    public ProgressDto(Long id, Long userId, Long bundleId, Long paperId, ProgressStatus status,
                       Float completionPercentage, Integer timeSpentMinutes, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bundleId = bundleId;
        this.paperId = paperId;
        this.status = status;
        this.completionPercentage = completionPercentage;
        this.timeSpentMinutes = timeSpentMinutes;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBundleId() { return bundleId; }
    public void setBundleId(Long bundleId) { this.bundleId = bundleId; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public ProgressStatus getStatus() { return status; }
    public void setStatus(ProgressStatus status) { this.status = status; }

    public Float getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Float completionPercentage) { this.completionPercentage = completionPercentage; }

    public Integer getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(Integer timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}