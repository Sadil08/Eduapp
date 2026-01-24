package com.eduapp.backend.dto;

import java.util.List;

public class PaperAttemptDto extends PaperSummaryDto {
    private List<QuestionAttemptDto> questions;
    private Integer attemptsMade;
    private Integer maxAttempts;
    private Integer remainingAttempts;
    private Boolean canAttempt;
    private Long attemptId;
    private Long originBundleId;

    public PaperAttemptDto() {
        super();
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getOriginBundleId() {
        return originBundleId;
    }

    public void setOriginBundleId(Long originBundleId) {
        this.originBundleId = originBundleId;
    }

    private Long originCustomBundleId;

    public Long getOriginCustomBundleId() {
        return originCustomBundleId;
    }

    public void setOriginCustomBundleId(Long originCustomBundleId) {
        this.originCustomBundleId = originCustomBundleId;
    }

    public List<QuestionAttemptDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionAttemptDto> questions) {
        this.questions = questions;
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
