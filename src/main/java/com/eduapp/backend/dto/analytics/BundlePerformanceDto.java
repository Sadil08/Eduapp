package com.eduapp.backend.dto.analytics;

import java.math.BigDecimal;

/**
 * DTO for bundle performance analytics
 */
public class BundlePerformanceDto {
    private Long bundleId;
    private String bundleName;
    private Long totalPurchases;
    private BigDecimal totalRevenue;
    private Double averageCompletionRate;
    private Double averageScore;

    // Constructors
    public BundlePerformanceDto() {
    }

    public BundlePerformanceDto(Long bundleId, String bundleName, Long totalPurchases,
                                BigDecimal totalRevenue, Double averageCompletionRate,
                                Double averageScore) {
        this.bundleId = bundleId;
        this.bundleName = bundleName;
        this.totalPurchases = totalPurchases;
        this.totalRevenue = totalRevenue;
        this.averageCompletionRate = averageCompletionRate;
        this.averageScore = averageScore;
    }

    // Getters and Setters
    public Long getBundleId() {
        return bundleId;
    }

    public void setBundleId(Long bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public Long getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(Long totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getAverageCompletionRate() {
        return averageCompletionRate;
    }

    public void setAverageCompletionRate(Double averageCompletionRate) {
        this.averageCompletionRate = averageCompletionRate;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
}
