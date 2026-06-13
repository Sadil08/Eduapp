package com.eduapp.backend.dto;

/**
 * Cambridge-shareable aggregate for a paper. Contains ONLY anonymised aggregate numbers —
 * never identities or raw rows. When the consenting cohort is below the anonymisation
 * floor, {@code suppressed} is true and all statistics are null.
 */
public class GlobalScoreAggregateDto {
    private Long paperId;
    private boolean suppressed;
    private int minimumCohortSize;
    private Long cohortSize;     // null when suppressed
    private Double averageScore; // null when suppressed
    private Integer minScore;
    private Integer maxScore;

    public static GlobalScoreAggregateDto suppressed(Long paperId, int floor) {
        GlobalScoreAggregateDto d = new GlobalScoreAggregateDto();
        d.paperId = paperId;
        d.suppressed = true;
        d.minimumCohortSize = floor;
        return d;
    }

    public static GlobalScoreAggregateDto of(Long paperId, int floor, long cohortSize,
            double avg, int min, int max) {
        GlobalScoreAggregateDto d = new GlobalScoreAggregateDto();
        d.paperId = paperId;
        d.suppressed = false;
        d.minimumCohortSize = floor;
        d.cohortSize = cohortSize;
        d.averageScore = avg;
        d.minScore = min;
        d.maxScore = max;
        return d;
    }

    public Long getPaperId() { return paperId; }
    public boolean isSuppressed() { return suppressed; }
    public int getMinimumCohortSize() { return minimumCohortSize; }
    public Long getCohortSize() { return cohortSize; }
    public Double getAverageScore() { return averageScore; }
    public Integer getMinScore() { return minScore; }
    public Integer getMaxScore() { return maxScore; }
}
