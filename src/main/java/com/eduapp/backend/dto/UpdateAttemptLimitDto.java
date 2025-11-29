package com.eduapp.backend.dto;

/**
 * DTO for updating attempt limits for a user on a specific paper.
 * Allows admin to increase/decrease allowed attempts.
 */
public class UpdateAttemptLimitDto {
    private Long userId;
    private Long paperId;
    private int maxFreeAttempts;

    public UpdateAttemptLimitDto() {
    }

    public UpdateAttemptLimitDto(Long userId, Long paperId, int maxFreeAttempts) {
        this.userId = userId;
        this.paperId = paperId;
        this.maxFreeAttempts = maxFreeAttempts;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public int getMaxFreeAttempts() {
        return maxFreeAttempts;
    }

    public void setMaxFreeAttempts(int maxFreeAttempts) {
        this.maxFreeAttempts = maxFreeAttempts;
    }
}
