package com.eduapp.backend.dto;

import com.eduapp.backend.model.SchoolPaper;

import java.time.LocalDateTime;

public class SchoolPaperDto {
    private Long id;
    private Long schoolId;
    private Long classId;
    private String name;
    private String description;
    private String type;
    private String status;
    private Integer timeLimitMinutes;
    private LocalDateTime examWindowStart;
    private LocalDateTime examWindowEnd;
    private boolean resultsReleased;

    public static SchoolPaperDto from(SchoolPaper p) {
        SchoolPaperDto d = new SchoolPaperDto();
        d.id = p.getId();
        d.schoolId = p.getSchoolId();
        d.classId = p.getClassId();
        d.name = p.getName();
        d.description = p.getDescription();
        d.type = p.getType().name();
        d.status = p.getStatus().name();
        d.timeLimitMinutes = p.getTimeLimitMinutes();
        d.examWindowStart = p.getExamWindowStart();
        d.examWindowEnd = p.getExamWindowEnd();
        d.resultsReleased = p.isResultsReleased();
        return d;
    }

    public Long getId() { return id; }
    public Long getSchoolId() { return schoolId; }
    public Long getClassId() { return classId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public LocalDateTime getExamWindowStart() { return examWindowStart; }
    public LocalDateTime getExamWindowEnd() { return examWindowEnd; }
    public boolean isResultsReleased() { return resultsReleased; }
}
