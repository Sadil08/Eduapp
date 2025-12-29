package com.eduapp.backend.dto;

import com.eduapp.backend.model.ImprovementStatus;
import java.time.LocalDateTime;

public class ImprovementDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String improvementText;
    private String issueDescription;
    private ImprovementStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ImprovementDto() {
    }

    public ImprovementDto(Long id, Long userId, String userName, String userEmail,
            String improvementText, String issueDescription, ImprovementStatus status,
            String adminNotes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.improvementText = improvementText;
        this.issueDescription = issueDescription;
        this.status = status;
        this.adminNotes = adminNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getImprovementText() {
        return improvementText;
    }

    public void setImprovementText(String improvementText) {
        this.improvementText = improvementText;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public ImprovementStatus getStatus() {
        return status;
    }

    public void setStatus(ImprovementStatus status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
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
}
