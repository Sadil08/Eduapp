package com.eduapp.backend.dto;

import java.time.Instant;

public class AssignSchoolPaperRequest {
    private Long classId;
    // ISO-8601 UTC instants (the client sends Date.toISOString(), e.g. "2026-06-19T18:29:00.000Z").
    private Instant examWindowStart;
    private Instant examWindowEnd;
    private Integer timeLimitMinutes;

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Instant getExamWindowStart() { return examWindowStart; }
    public void setExamWindowStart(Instant examWindowStart) { this.examWindowStart = examWindowStart; }
    public Instant getExamWindowEnd() { return examWindowEnd; }
    public void setExamWindowEnd(Instant examWindowEnd) { this.examWindowEnd = examWindowEnd; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
}
