package com.eduapp.backend.dto;

import java.math.BigDecimal;

/**
 * DTO for overall system statistics.
 * Used by admin dashboard to display system-wide metrics.
 */
public class SystemStatsDto {
    private int totalBundles;
    private int totalPapers;
    private int totalQuestions;
    private int totalUsers;
    private int totalAttempts;
    private BigDecimal totalRevenue;

    public SystemStatsDto() {
    }

    public SystemStatsDto(int totalBundles, int totalPapers, int totalQuestions,
            int totalUsers, int totalAttempts, BigDecimal totalRevenue) {
        this.totalBundles = totalBundles;
        this.totalPapers = totalPapers;
        this.totalQuestions = totalQuestions;
        this.totalUsers = totalUsers;
        this.totalAttempts = totalAttempts;
        this.totalRevenue = totalRevenue;
    }

    public int getTotalBundles() {
        return totalBundles;
    }

    public void setTotalBundles(int totalBundles) {
        this.totalBundles = totalBundles;
    }

    public int getTotalPapers() {
        return totalPapers;
    }

    public void setTotalPapers(int totalPapers) {
        this.totalPapers = totalPapers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
