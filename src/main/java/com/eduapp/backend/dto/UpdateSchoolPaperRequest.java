package com.eduapp.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Edit a DRAFT school paper's metadata. Type is immutable after creation. */
public class UpdateSchoolPaperRequest {
    @NotBlank(message = "Paper name is required")
    private String name;
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
