package com.eduapp.backend.dto;

import com.eduapp.backend.model.PaperType;

public class CreateSchoolPaperRequest {
    private String name;
    private String description;
    private PaperType type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PaperType getType() { return type; }
    public void setType(PaperType type) { this.type = type; }
}
