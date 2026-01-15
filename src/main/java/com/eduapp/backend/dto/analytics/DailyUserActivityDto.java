package com.eduapp.backend.dto.analytics;

import java.time.LocalDate;

/**
 * DTO for daily user registration counts
 */
public class DailyUserActivityDto {
    private LocalDate date;
    private Long newRegistrations;
    private Long activeUsers;
    private Long totalLogins;

    // Constructors
    public DailyUserActivityDto() {
    }

    public DailyUserActivityDto(LocalDate date, Long newRegistrations, Long activeUsers, Long totalLogins) {
        this.date = date;
        this.newRegistrations = newRegistrations;
        this.activeUsers = activeUsers;
        this.totalLogins = totalLogins;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getNewRegistrations() {
        return newRegistrations;
    }

    public void setNewRegistrations(Long newRegistrations) {
        this.newRegistrations = newRegistrations;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(Long totalLogins) {
        this.totalLogins = totalLogins;
    }
}
