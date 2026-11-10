package com.eduapp.backend.dto;

import com.eduapp.backend.model.SchoolClass;
import com.eduapp.backend.model.SchoolEnrolment;
import com.eduapp.backend.model.User;

public class SchoolEnrolmentDto {
    private Long id;
    private Long schoolId;
    private Long classId;
    private Long studentId;
    private String studentName;  // BT-9: shown on the teacher review screen
    private String studentEmail;
    private String className;     // shown on the student's "my classes" dashboard
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

    /** BT-9: enriched with the student's identity for review screens. */
    public static SchoolEnrolmentDto from(SchoolEnrolment e, User student) {
        SchoolEnrolmentDto d = from(e);
        if (student != null) {
            d.studentName = student.getUsername();
            d.studentEmail = student.getEmail();
        }
        return d;
    }

    /** Enriched with the class name for the student's own enrolment list. */
    public static SchoolEnrolmentDto fromWithClass(SchoolEnrolment e, SchoolClass cls) {
        SchoolEnrolmentDto d = from(e);
        if (cls != null) {
            d.className = cls.getName();
        }
        return d;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassName() {
        return className;
    }

    public String getStudentEmail() {
        return studentEmail;
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
