package com.eduapp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateInviteRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
