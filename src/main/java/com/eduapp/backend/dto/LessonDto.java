package com.eduapp.backend.dto;

public class LessonDto {
    private Long id;
    private String name;
    private String description;
    private Long subjectId;

    public LessonDto() {
    }

    public LessonDto(Long id, String name, String description, Long subjectId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.subjectId = subjectId;
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

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
}
