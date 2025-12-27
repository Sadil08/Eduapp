// src/main/java/com/eduapp/backend/dto/RegisterRequest.java
package com.eduapp.backend.dto;

import com.eduapp.backend.model.Role;

public class RegisterRequest {
    private String email;
    private String password;
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