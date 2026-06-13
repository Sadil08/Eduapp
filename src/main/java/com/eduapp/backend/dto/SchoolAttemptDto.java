package com.eduapp.backend.dto;

import com.eduapp.backend.model.SchoolPaperAttempt;

import java.time.LocalDateTime;

/** Teacher-facing view of an attempt (shows AI mark, override and the effective mark). */
public class SchoolAttemptDto {
    private Long id;
    private Long studentId;
    private Long schoolPaperId;
    private String status;
    private Integer aiMark;
    private Integer teacherOverrideMark;
    private String teacherOverrideNote;
    private Integer effectiveMark;
    private LocalDateTime teacherReviewedAt;
    private Integer timeTakenMinutes;

    public static SchoolAttemptDto from(SchoolPaperAttempt a) {
        SchoolAttemptDto d = new SchoolAttemptDto();
        d.id = a.getId();
        d.studentId = a.getStudentId();
        d.schoolPaperId = a.getSchoolPaperId();
        d.status = a.getStatus().name();
        d.aiMark = a.getAiMark();
        d.teacherOverrideMark = a.getTeacherOverrideMark();
        d.teacherOverrideNote = a.getTeacherOverrideNote();
        d.effectiveMark = a.getEffectiveMark();
        d.teacherReviewedAt = a.getTeacherReviewedAt();
        d.timeTakenMinutes = a.getTimeTakenMinutes();
        return d;
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public Long getSchoolPaperId() { return schoolPaperId; }
    public String getStatus() { return status; }
    public Integer getAiMark() { return aiMark; }
    public Integer getTeacherOverrideMark() { return teacherOverrideMark; }
    public String getTeacherOverrideNote() { return teacherOverrideNote; }
    public Integer getEffectiveMark() { return effectiveMark; }
    public LocalDateTime getTeacherReviewedAt() { return teacherReviewedAt; }
    public Integer getTimeTakenMinutes() { return timeTakenMinutes; }
}
