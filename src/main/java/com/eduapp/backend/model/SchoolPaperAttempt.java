package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A student's sitting of a {@link SchoolPaper}. Tenant-scoped via {@code school_id}.
 * Holds both the AI mark and the teacher's optional override (human-in-the-loop,
 * PRODUCT_BUSINESS_PLAN.md §5.3). The effective/gradebook mark is the override when set,
 * otherwise the AI mark.
 */
@Entity
@Table(name = "school_paper_attempts",
        uniqueConstraints = @UniqueConstraint(name = "uk_school_attempt_active",
                columnNames = {"school_paper_id", "student_id"}))
public class SchoolPaperAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "school_paper_id", nullable = false)
    private Long schoolPaperId;

    @Column(name = "class_id")
    private Long classId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "time_taken_minutes")
    private Integer timeTakenMinutes;

    @Column(name = "ai_mark")
    private Integer aiMark;

    @Column(name = "teacher_override_mark")
    private Integer teacherOverrideMark;

    @Column(name = "teacher_override_note", length = 1000)
    private String teacherOverrideNote;

    @Column(name = "teacher_reviewed_at")
    private LocalDateTime teacherReviewedAt;

    public SchoolPaperAttempt() {
    }

    public SchoolPaperAttempt(Long schoolId, Long studentId, Long schoolPaperId, Long classId) {
        this.schoolId = schoolId;
        this.studentId = studentId;
        this.schoolPaperId = schoolPaperId;
        this.classId = classId;
    }

    /** The mark that counts in the gradebook: teacher override if present, else the AI mark. */
    public Integer getEffectiveMark() {
        return teacherOverrideMark != null ? teacherOverrideMark : aiMark;
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSchoolPaperId() {
        return schoolPaperId;
    }

    public void setSchoolPaperId(Long schoolPaperId) {
        this.schoolPaperId = schoolPaperId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTimeTakenMinutes() {
        return timeTakenMinutes;
    }

    public void setTimeTakenMinutes(Integer timeTakenMinutes) {
        this.timeTakenMinutes = timeTakenMinutes;
    }

    public Integer getAiMark() {
        return aiMark;
    }

    public void setAiMark(Integer aiMark) {
        this.aiMark = aiMark;
    }

    public Integer getTeacherOverrideMark() {
        return teacherOverrideMark;
    }

    public void setTeacherOverrideMark(Integer teacherOverrideMark) {
        this.teacherOverrideMark = teacherOverrideMark;
    }

    public String getTeacherOverrideNote() {
        return teacherOverrideNote;
    }

    public void setTeacherOverrideNote(String teacherOverrideNote) {
        this.teacherOverrideNote = teacherOverrideNote;
    }

    public LocalDateTime getTeacherReviewedAt() {
        return teacherReviewedAt;
    }

    public void setTeacherReviewedAt(LocalDateTime teacherReviewedAt) {
        this.teacherReviewedAt = teacherReviewedAt;
    }
}
