package com.eduapp.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class JoinClassRequest {
    @NotBlank(message = "Class code is required")
    private String classCode;

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }
}
