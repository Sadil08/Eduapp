package com.eduapp.backend.dto.analytics;

import java.util.Map;

/**
 * DTO for extraction usage statistics
 */
public class ExtractionStatsDto {
    private Long totalExtractions;
    private Long uniqueUsersWithExtractions;
    private Long extractionsToday;
    private Map<Long, Long> extractionsByUser; // userId -> count
    private Long usersHitLimit;

    // Constructors
    public ExtractionStatsDto() {
    }

    public ExtractionStatsDto(Long totalExtractions, Long uniqueUsersWithExtractions, 
                              Long extractionsToday, Map<Long, Long> extractionsByUser, 
                              Long usersHitLimit) {
        this.totalExtractions = totalExtractions;
        this.uniqueUsersWithExtractions = uniqueUsersWithExtractions;
        this.extractionsToday = extractionsToday;
        this.extractionsByUser = extractionsByUser;
        this.usersHitLimit = usersHitLimit;
    }

    // Getters and Setters
    public Long getTotalExtractions() {
        return totalExtractions;
    }

    public void setTotalExtractions(Long totalExtractions) {
        this.totalExtractions = totalExtractions;
    }

    public Long getUniqueUsersWithExtractions() {
        return uniqueUsersWithExtractions;
    }

    public void setUniqueUsersWithExtractions(Long uniqueUsersWithExtractions) {
        this.uniqueUsersWithExtractions = uniqueUsersWithExtractions;
    }

    public Long getExtractionsToday() {
        return extractionsToday;
    }

    public void setExtractionsToday(Long extractionsToday) {
        this.extractionsToday = extractionsToday;
    }

    public Map<Long, Long> getExtractionsByUser() {
        return extractionsByUser;
    }

    public void setExtractionsByUser(Map<Long, Long> extractionsByUser) {
        this.extractionsByUser = extractionsByUser;
    }

    public Long getUsersHitLimit() {
        return usersHitLimit;
    }

    public void setUsersHitLimit(Long usersHitLimit) {
        this.usersHitLimit = usersHitLimit;
    }
}
