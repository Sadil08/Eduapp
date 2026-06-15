// src/main/java/com/eduapp/backend/dto/RegisterRequest.java
package com.eduapp.backend.dto;

import com.eduapp.backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    // N-S4: minimum password length for a platform handling children's data.
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    private Role role;
    private String referralCode;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String name, Role role) {
        this(email, password, name, role, null);
    }

    public RegisterRequest(String email, String password, String name, Role role, String referralCode) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.referralCode = referralCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
}