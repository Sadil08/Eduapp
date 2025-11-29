package com.eduapp.backend.dto;

/**
 * DTO for bundle-specific statistics.
 * Provides detailed metrics for a single paper bundle.
 */
public class BundleStatsDto {
    private Long bundleId;
    private String bundleName;
    private int totalPapers;
    private int totalQuestions;
    private int totalStudentsWithAccess;
    private int totalAttempts;

    public BundleStatsDto() {
    }

    public BundleStatsDto(Long bundleId, String bundleName, int totalPapers,
            int totalQuestions, int totalStudentsWithAccess, int totalAttempts) {
        this.bundleId = bundleId;
        this.bundleName = bundleName;
        this.totalPapers = totalPapers;
        this.totalQuestions = totalQuestions;
        this.totalStudentsWithAccess = totalStudentsWithAccess;
        this.totalAttempts = totalAttempts;
    }

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

    public int getTotalStudentsWithAccess() {
        return totalStudentsWithAccess;
    }

    public void setTotalStudentsWithAccess(int totalStudentsWithAccess) {
        this.totalStudentsWithAccess = totalStudentsWithAccess;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }
}
