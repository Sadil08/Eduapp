package com.eduapp.backend.dto;

import com.eduapp.backend.model.SchoolClass;

/** Response/request DTO for school classes (never exposes cross-tenant internals beyond the owning school). */
public class SchoolClassDto {

    private Long id;
    private Long schoolId;
    private Long teacherId;
    private String name;
    private String subject;
    private String yearGroup;
    private String classCode;

    public SchoolClassDto() {
    }

    public static SchoolClassDto from(SchoolClass sc) {
        SchoolClassDto d = new SchoolClassDto();
        d.id = sc.getId();
        d.schoolId = sc.getSchoolId();
        d.teacherId = sc.getTeacherId();
        d.name = sc.getName();
        d.subject = sc.getSubject();
        d.yearGroup = sc.getYearGroup();
        d.classCode = sc.getClassCode();
        return d;
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolId() {
        return schoolId;
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
}
