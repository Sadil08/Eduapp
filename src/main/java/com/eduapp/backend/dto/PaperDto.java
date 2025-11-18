package com.eduapp.backend.dto;

import com.eduapp.backend.model.PaperType;
import java.util.List;

public class PaperDto {
    private Long id;
    private String name;
    private String description;
    private PaperType type;
    private Long bundleId;
    private Integer maxFreeAttempts;
    private List<QuestionDto> questions;

    public PaperDto() {}

    public PaperDto(Long id, String name, String description, PaperType type, Long bundleId, Integer maxFreeAttempts, List<QuestionDto> questions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.bundleId = bundleId;
        this.maxFreeAttempts = maxFreeAttempts;
        this.questions = questions;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PaperType getType() { return type; }
    public void setType(PaperType type) { this.type = type; }

    public Long getBundleId() { return bundleId; }
    public void setBundleId(Long bundleId) { this.bundleId = bundleId; }

    public Integer getMaxFreeAttempts() { return maxFreeAttempts; }
    public void setMaxFreeAttempts(Integer maxFreeAttempts) { this.maxFreeAttempts = maxFreeAttempts; }

    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }
}
