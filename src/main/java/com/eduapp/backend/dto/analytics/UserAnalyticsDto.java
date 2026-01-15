package com.eduapp.backend.dto.analytics;

import com.eduapp.backend.model.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for individual user analytics data
 */
public class UserAnalyticsDto {
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private String country;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginTime;
    private Long totalBundlesPurchased;
    private BigDecimal totalSpent;
    private Long totalExtractions;
    private Long totalPaperAttempts;
    private Double averageScore;

    // Constructors
    public UserAnalyticsDto() {
    }

    public UserAnalyticsDto(Long userId, String username, String email, Role role, String country,
                            LocalDateTime registrationDate, LocalDateTime lastLoginTime,
                            Long totalBundlesPurchased, BigDecimal totalSpent, Long totalExtractions,
                            Long totalPaperAttempts, Double averageScore) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.country = country;
        this.registrationDate = registrationDate;
        this.lastLoginTime = lastLoginTime;
        this.totalBundlesPurchased = totalBundlesPurchased;
        this.totalSpent = totalSpent;
        this.totalExtractions = totalExtractions;
        this.totalPaperAttempts = totalPaperAttempts;
        this.averageScore = averageScore;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Long getTotalBundlesPurchased() {
        return totalBundlesPurchased;
    }

    public void setTotalBundlesPurchased(Long totalBundlesPurchased) {
        this.totalBundlesPurchased = totalBundlesPurchased;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Long getTotalExtractions() {
        return totalExtractions;
    }

    public void setTotalExtractions(Long totalExtractions) {
        this.totalExtractions = totalExtractions;
    }

    public Long getTotalPaperAttempts() {
        return totalPaperAttempts;
    }

    public void setTotalPaperAttempts(Long totalPaperAttempts) {
        this.totalPaperAttempts = totalPaperAttempts;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
}
