package com.eduapp.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

/**
 * A class/section within a school (e.g. "Grade 10 - Physics - Section A").
 * First tenant-scoped entity: every row carries {@code school_id} and is automatically
 * restricted to the current tenant by the Hibernate {@code tenantFilter} (enabled per
 * request in {@code TenantFilter} when a tenant is present).
 */
@Entity
@Table(name = "school_classes",
        uniqueConstraints = @UniqueConstraint(name = "uk_school_class_code", columnNames = "class_code"))
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "schoolId", type = Long.class))
@Filter(name = "tenantFilter", condition = "school_id = :schoolId")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(nullable = false)
    private String name;

    @Column
    private String subject;

    @Column(name = "year_group")
    private String yearGroup;

    @Column(name = "class_code", nullable = false)
    private String classCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SchoolClass() {
    }

    public SchoolClass(Long schoolId, Long teacherId, String name, String subject, String yearGroup, String classCode) {
        this.schoolId = schoolId;
        this.teacherId = teacherId;
        this.name = name;
        this.subject = subject;
        this.yearGroup = yearGroup;
        this.classCode = classCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getYearGroup() {
        return yearGroup;
    }

    public void setYearGroup(String yearGroup) {
        this.yearGroup = yearGroup;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
