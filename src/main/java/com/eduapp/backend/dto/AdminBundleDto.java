package com.eduapp.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin bundle management.
 * Extends PaperBundleDto with additional admin-specific information.
 */
public class AdminBundleDto extends PaperBundleDto {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private BundleStatsDto stats;

    public AdminBundleDto() {
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

    public BundleStatsDto getStats() {
        return stats;
    }

    public void setStats(BundleStatsDto stats) {
        this.stats = stats;
    }
}
