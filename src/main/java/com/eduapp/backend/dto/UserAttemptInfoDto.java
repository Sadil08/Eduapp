package com.eduapp.backend.dto;

/**
 * DTO for user's attempt information for a specific paper.
 * Shows how many attempts made and remaining.
 */
public class UserAttemptInfoDto {
    private Long userId;
    private Long paperId;
    private String paperName;
    private int attemptsMade;
    private int maxFreeAttempts;
    private int remainingAttempts;

    public UserAttemptInfoDto() {
    }

    public UserAttemptInfoDto(Long userId, Long paperId, String paperName,
            int attemptsMade, int maxFreeAttempts) {
        this.userId = userId;
        this.paperId = paperId;
        this.paperName = paperName;
        this.attemptsMade = attemptsMade;
        this.maxFreeAttempts = maxFreeAttempts;
        this.remainingAttempts = Math.max(0, maxFreeAttempts - attemptsMade);
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

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public int getAttemptsMade() {
        return attemptsMade;
    }

    public void setAttemptsMade(int attemptsMade) {
        this.attemptsMade = attemptsMade;
        this.remainingAttempts = Math.max(0, this.maxFreeAttempts - attemptsMade);
    }

    public int getMaxFreeAttempts() {
        return maxFreeAttempts;
    }

    public void setMaxFreeAttempts(int maxFreeAttempts) {
        this.maxFreeAttempts = maxFreeAttempts;
        this.remainingAttempts = Math.max(0, maxFreeAttempts - this.attemptsMade);
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }
}
