package com.eduapp.backend.dto;

import java.time.LocalDateTime;

public class AssignSchoolPaperRequest {
    private Long classId;
    private LocalDateTime examWindowStart;
    private LocalDateTime examWindowEnd;
    private Integer timeLimitMinutes;

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public LocalDateTime getExamWindowStart() { return examWindowStart; }
    public void setExamWindowStart(LocalDateTime examWindowStart) { this.examWindowStart = examWindowStart; }
    public LocalDateTime getExamWindowEnd() { return examWindowEnd; }
    public void setExamWindowEnd(LocalDateTime examWindowEnd) { this.examWindowEnd = examWindowEnd; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
}
