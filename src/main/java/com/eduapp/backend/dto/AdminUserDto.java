package com.eduapp.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin user management.
 * Contains user information and statistics for admin dashboard.
 */
public class AdminUserDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private int totalBundlesPurchased;
    private int totalAttempts;

    public AdminUserDto() {
    }

    public AdminUserDto(Long id, String username, String email, String role,
            LocalDateTime createdAt, int totalBundlesPurchased, int totalAttempts) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.totalBundlesPurchased = totalBundlesPurchased;
        this.totalAttempts = totalAttempts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getTotalBundlesPurchased() {
        return totalBundlesPurchased;
    }

    public void setTotalBundlesPurchased(int totalBundlesPurchased) {
        this.totalBundlesPurchased = totalBundlesPurchased;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }
}
