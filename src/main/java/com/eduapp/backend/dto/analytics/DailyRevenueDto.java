package com.eduapp.backend.dto.analytics;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for daily revenue statistics
 */
public class DailyRevenueDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate date;
    private BigDecimal revenue;
    private Long transactions;
    private Long uniqueUsers;

    // Constructors
    public DailyRevenueDto() {
    }

    public DailyRevenueDto(LocalDate date, BigDecimal revenue, Long transactions, Long uniqueUsers) {
        this.date = date;
        this.revenue = revenue;
        this.transactions = transactions;
        this.uniqueUsers = uniqueUsers;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Long getTransactions() {
        return transactions;
    }

    public void setTransactions(Long transactions) {
        this.transactions = transactions;
    }

    public Long getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(Long uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }
}
