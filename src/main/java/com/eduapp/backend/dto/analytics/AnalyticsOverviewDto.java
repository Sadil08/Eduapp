package com.eduapp.backend.dto.analytics;

import java.math.BigDecimal;

import java.io.Serializable;

/**
 * DTO for analytics dashboard overview metrics
 */
public class AnalyticsOverviewDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long totalUsers;
    private Long activeUsersToday;
    private Long activeUsersThisMonth;
    private BigDecimal totalRevenue;
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private Long totalExtractions;
    private Long extractionsToday;
    private Long totalBundlesSold;
    private Integer totalCountries;

    // Constructors
    public AnalyticsOverviewDto() {
    }

    public AnalyticsOverviewDto(Long totalUsers, Long activeUsersToday, Long activeUsersThisMonth,
                                BigDecimal totalRevenue, BigDecimal revenueToday, BigDecimal revenueThisMonth,
                                Long totalExtractions, Long extractionsToday, Long totalBundlesSold,
                                Integer totalCountries) {
        this.totalUsers = totalUsers;
        this.activeUsersToday = activeUsersToday;
        this.activeUsersThisMonth = activeUsersThisMonth;
        this.totalRevenue = totalRevenue;
        this.revenueToday = revenueToday;
        this.revenueThisMonth = revenueThisMonth;
        this.totalExtractions = totalExtractions;
        this.extractionsToday = extractionsToday;
        this.totalBundlesSold = totalBundlesSold;
        this.totalCountries = totalCountries;
    }

    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveUsersToday() {
        return activeUsersToday;
    }

    public void setActiveUsersToday(Long activeUsersToday) {
        this.activeUsersToday = activeUsersToday;
    }

    public Long getActiveUsersThisMonth() {
        return activeUsersThisMonth;
    }

    public void setActiveUsersThisMonth(Long activeUsersThisMonth) {
        this.activeUsersThisMonth = activeUsersThisMonth;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getRevenueToday() {
        return revenueToday;
    }

    public void setRevenueToday(BigDecimal revenueToday) {
        this.revenueToday = revenueToday;
    }

    public BigDecimal getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public void setRevenueThisMonth(BigDecimal revenueThisMonth) {
        this.revenueThisMonth = revenueThisMonth;
    }

    public Long getTotalExtractions() {
        return totalExtractions;
    }

    public void setTotalExtractions(Long totalExtractions) {
        this.totalExtractions = totalExtractions;
    }

    public Long getExtractionsToday() {
        return extractionsToday;
    }

    public void setExtractionsToday(Long extractionsToday) {
        this.extractionsToday = extractionsToday;
    }

    public Long getTotalBundlesSold() {
        return totalBundlesSold;
    }

    public void setTotalBundlesSold(Long totalBundlesSold) {
        this.totalBundlesSold = totalBundlesSold;
    }

    public Integer getTotalCountries() {
        return totalCountries;
    }

    public void setTotalCountries(Integer totalCountries) {
        this.totalCountries = totalCountries;
    }
}
