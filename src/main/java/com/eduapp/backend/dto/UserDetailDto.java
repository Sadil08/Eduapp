package com.eduapp.backend.dto;

import com.eduapp.backend.model.Role;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed user information used by admins to monitor user activity.
 * Includes user profile, accessed bundles, attempted papers, progress, scores, and AI feedback summaries.
 */
public class UserDetailDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PaperBundleDto> accessedBundles;
    private List<StudentPaperAttemptDto> attemptedPapers;
    private List<ProgressDto> progress;
    private List<LeaderboardEntryDto> scores;
    private List<AIAnalysisDto> aiFeedbackSummaries;

    public UserDetailDto() {}

    public UserDetailDto(Long id, String username, String email, Role role, LocalDateTime createdAt, LocalDateTime updatedAt,
                         List<PaperBundleDto> accessedBundles, List<StudentPaperAttemptDto> attemptedPapers,
                         List<ProgressDto> progress, List<LeaderboardEntryDto> scores, List<AIAnalysisDto> aiFeedbackSummaries) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.accessedBundles = accessedBundles;
        this.attemptedPapers = attemptedPapers;
        this.progress = progress;
        this.scores = scores;
        this.aiFeedbackSummaries = aiFeedbackSummaries;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PaperBundleDto> getAccessedBundles() { return accessedBundles; }
    public void setAccessedBundles(List<PaperBundleDto> accessedBundles) { this.accessedBundles = accessedBundles; }

    public List<StudentPaperAttemptDto> getAttemptedPapers() { return attemptedPapers; }
    public void setAttemptedPapers(List<StudentPaperAttemptDto> attemptedPapers) { this.attemptedPapers = attemptedPapers; }

    public List<ProgressDto> getProgress() { return progress; }
    public void setProgress(List<ProgressDto> progress) { this.progress = progress; }

    public List<LeaderboardEntryDto> getScores() { return scores; }
    public void setScores(List<LeaderboardEntryDto> scores) { this.scores = scores; }

    public List<AIAnalysisDto> getAiFeedbackSummaries() { return aiFeedbackSummaries; }
    public void setAiFeedbackSummaries(List<AIAnalysisDto> aiFeedbackSummaries) { this.aiFeedbackSummaries = aiFeedbackSummaries; }
}