package com.eduapp.backend.dto;

import java.util.List;

public class SubjectDto {
    private Long id;
    private String name;
    private String description;
    private List<LessonDto> lessons; // optional for nested views

    public SubjectDto() {
    }

    public SubjectDto(Long id, String name, String description, List<LessonDto> lessons) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lessons = lessons;
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

    public List<LessonDto> getLessons() {
        return lessons;
    }

    public void setLessons(List<LessonDto> lessons) {
        this.lessons = lessons;
    }
}
