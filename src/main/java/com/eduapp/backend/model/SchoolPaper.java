package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A school-tier internal assessment (PRODUCT_BUSINESS_PLAN.md §5.2). Tenant-scoped via
 * {@code school_id}. Its questions are shared {@link Question} rows (so the existing AI
 * extraction + marking pipeline is reused) carrying this paper's id in {@code school_paper_id}.
 *
 * Lifecycle: DRAFT (teacher adds/reviews questions) → APPROVED (assignable) → assigned to
 * a class with an exam window. Students only ever see APPROVED + assigned papers.
 */
@Entity
@Table(name = "school_papers")
public class SchoolPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    // Null until the paper is assigned to a class.
    @Column(name = "class_id")
    private Long classId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperType type = PaperType.MIXED;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SchoolPaperStatus status = SchoolPaperStatus.DRAFT;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "exam_window_start")
    private LocalDateTime examWindowStart;

    @Column(name = "exam_window_end")
    private LocalDateTime examWindowEnd;

    @Column(name = "results_released", nullable = false)
    private boolean resultsReleased = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SchoolPaper() {
    }

    public SchoolPaper(Long schoolId, String name, String description, PaperType type, Long createdBy) {
        this.schoolId = schoolId;
        this.name = name;
        this.description = description;
        this.type = type != null ? type : PaperType.MIXED;
        this.createdBy = createdBy;
    }

    /** True only when a student is allowed to sit this paper right now. */
    public boolean isOpenForAttempts(LocalDateTime now) {
        if (status != SchoolPaperStatus.ASSIGNED) {
            return false;
        }
        boolean afterStart = examWindowStart == null || !now.isBefore(examWindowStart);
        boolean beforeEnd = examWindowEnd == null || !now.isAfter(examWindowEnd);
        return afterStart && beforeEnd;
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

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
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

    public SchoolPaperStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolPaperStatus status) {
        this.status = status;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public LocalDateTime getExamWindowStart() {
        return examWindowStart;
    }

    public void setExamWindowStart(LocalDateTime examWindowStart) {
        this.examWindowStart = examWindowStart;
    }

    public LocalDateTime getExamWindowEnd() {
        return examWindowEnd;
    }

    public void setExamWindowEnd(LocalDateTime examWindowEnd) {
        this.examWindowEnd = examWindowEnd;
    }

    public boolean isResultsReleased() {
        return resultsReleased;
    }

    public void setResultsReleased(boolean resultsReleased) {
        this.resultsReleased = resultsReleased;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
