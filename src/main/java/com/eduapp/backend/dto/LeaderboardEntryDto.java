package com.eduapp.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for leaderboard entries showing user scores.
 */
public class LeaderboardEntryDto {
    private Long id;
    private Long userId;
    private Long paperId;
    private Integer score;
    private Long subjectId;
    private Boolean isAnonymous;
    private LocalDateTime createdAt;

    public LeaderboardEntryDto() {}

    public LeaderboardEntryDto(Long id, Long userId, Long paperId, Integer score, Long subjectId,
                               Boolean isAnonymous, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.paperId = paperId;
        this.score = score;
        this.subjectId = subjectId;
        this.isAnonymous = isAnonymous;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}