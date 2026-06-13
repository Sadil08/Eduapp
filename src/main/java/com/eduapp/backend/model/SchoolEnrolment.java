package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A student's membership in a {@link SchoolClass}. Tenant-scoped via {@code school_id}.
 * A student may only have one active enrolment per class (unique constraint).
 */
@Entity
@Table(name = "school_enrolments",
        uniqueConstraints = @UniqueConstraint(name = "uk_enrolment_class_student",
                columnNames = {"class_id", "student_id"}))
public class SchoolEnrolment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SchoolEnrolmentStatus status = SchoolEnrolmentStatus.ACTIVE;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    public SchoolEnrolment() {
    }

    public SchoolEnrolment(Long schoolId, Long classId, Long studentId) {
        this.schoolId = schoolId;
        this.classId = classId;
        this.studentId = studentId;
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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public SchoolEnrolmentStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolEnrolmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }
}
