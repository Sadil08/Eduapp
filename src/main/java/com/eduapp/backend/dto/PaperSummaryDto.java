package com.eduapp.backend.dto;

import com.eduapp.backend.model.PaperType;
import java.util.List;

public class PaperSummaryDto {
    private Long id;
    private String name;
    private String description;
    private PaperType type;
    private List<Long> bundleIds;
    private Long subjectId;
    private String subjectName;
    private Integer maxFreeAttempts;
    private Integer totalMarks;
    private String videoUrl;

    public PaperSummaryDto() {
    }

    public PaperSummaryDto(Long id, String name, String description, PaperType type, List<Long> bundleIds,
            Integer maxFreeAttempts, Integer totalMarks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.bundleIds = bundleIds;
        this.maxFreeAttempts = maxFreeAttempts;
        this.totalMarks = totalMarks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaperType getType() {
        return type;
    }

    public void setType(PaperType type) {
        this.type = type;
    }

    public List<Long> getBundleIds() {
        return bundleIds;
    }

    public void setBundleIds(List<Long> bundleIds) {
        this.bundleIds = bundleIds;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getMaxFreeAttempts() {
        return maxFreeAttempts;
    }

    public void setMaxFreeAttempts(Integer maxFreeAttempts) {
        this.maxFreeAttempts = maxFreeAttempts;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
