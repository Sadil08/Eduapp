package com.eduapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ImprovementSubmissionDto {

    @NotBlank(message = "Improvement suggestion is required")
    @Size(min = 20, max = 1000, message = "Improvement text must be between 20 and 1000 characters")
    private String improvementText;

    @Size(max = 1000, message = "Issue description must not exceed 1000 characters")
    private String issueDescription;

    // Constructors
    public ImprovementSubmissionDto() {
    }

    public ImprovementSubmissionDto(String improvementText, String issueDescription) {
        this.improvementText = improvementText;
        this.issueDescription = issueDescription;
    }

    // Getters and Setters
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
}
