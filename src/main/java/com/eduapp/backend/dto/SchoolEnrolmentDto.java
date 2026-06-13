package com.eduapp.backend.dto;

import com.eduapp.backend.model.SchoolEnrolment;

public class SchoolEnrolmentDto {
    private Long id;
    private Long schoolId;
    private Long classId;
    private Long studentId;
    private String status;

    public static SchoolEnrolmentDto from(SchoolEnrolment e) {
        SchoolEnrolmentDto d = new SchoolEnrolmentDto();
        d.id = e.getId();
        d.schoolId = e.getSchoolId();
        d.classId = e.getClassId();
        d.studentId = e.getStudentId();
        d.status = e.getStatus().name();
        return d;
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public Long getClassId() {
        return classId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStatus() {
        return status;
    }
}
