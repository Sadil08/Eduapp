package com.eduapp.backend.dto.analytics;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for geographical user statistics
 */
public class GeographicalStatsDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String country;
    private Long userCount;
    private Long activeUsers;
    private BigDecimal revenue;
    private Long totalExtractions;

    // Constructors
    public GeographicalStatsDto() {
    }

    public GeographicalStatsDto(String country, Long userCount, Long activeUsers, 
                                BigDecimal revenue, Long totalExtractions) {
        this.country = country;
        this.userCount = userCount;
        this.activeUsers = activeUsers;
        this.revenue = revenue;
        this.totalExtractions = totalExtractions;
    }

    // Getters and Setters
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Long getTotalExtractions() {
        return totalExtractions;
    }

    public void setTotalExtractions(Long totalExtractions) {
        this.totalExtractions = totalExtractions;
    }
}
