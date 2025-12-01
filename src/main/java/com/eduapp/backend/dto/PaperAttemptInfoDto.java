package com.eduapp.backend.dto;

/**
 * DTO for paper attempt information
 * Contains attempt counts and limits for a specific paper and user
 */
public class PaperAttemptInfoDto {
    private Long paperId;
    private Integer attemptsMade;
    private Integer maxAttempts;
    private Integer remainingAttempts;
    private Boolean canAttempt;

    public PaperAttemptInfoDto() {
    }

    public PaperAttemptInfoDto(Long paperId, Integer attemptsMade, Integer maxAttempts,
            Integer remainingAttempts, Boolean canAttempt) {
        this.paperId = paperId;
        this.attemptsMade = attemptsMade;
        this.maxAttempts = maxAttempts;
        this.remainingAttempts = remainingAttempts;
        this.canAttempt = canAttempt;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public Integer getAttemptsMade() {
        return attemptsMade;
    }

    public void setAttemptsMade(Integer attemptsMade) {
        this.attemptsMade = attemptsMade;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(Integer remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    public Boolean getCanAttempt() {
        return canAttempt;
    }

    public void setCanAttempt(Boolean canAttempt) {
        this.canAttempt = canAttempt;
    }
}
