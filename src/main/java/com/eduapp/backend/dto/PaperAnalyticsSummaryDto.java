package com.eduapp.backend.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Cohort summary for a school paper. Aggregate only (counts/averages/distribution) — no
 * per-student rows. Serializable so it can be cached.
 */
public class PaperAnalyticsSummaryDto implements Serializable {
    private Long schoolPaperId;
    private long attemptCount;
    private long submittedCount;
    private Double averageMark;
    private Integer highestMark;
    private Integer lowestMark;
    private Map<Integer, Long> markDistribution; // effective mark -> number of students

    public PaperAnalyticsSummaryDto() {
    }

    public PaperAnalyticsSummaryDto(Long schoolPaperId, long attemptCount, long submittedCount,
            Double averageMark, Integer highestMark, Integer lowestMark, Map<Integer, Long> markDistribution) {
        this.schoolPaperId = schoolPaperId;
        this.attemptCount = attemptCount;
        this.submittedCount = submittedCount;
        this.averageMark = averageMark;
        this.highestMark = highestMark;
        this.lowestMark = lowestMark;
        this.markDistribution = markDistribution;
    }

    public Long getSchoolPaperId() { return schoolPaperId; }
    public long getAttemptCount() { return attemptCount; }
    public long getSubmittedCount() { return submittedCount; }
    public Double getAverageMark() { return averageMark; }
    public Integer getHighestMark() { return highestMark; }
    public Integer getLowestMark() { return lowestMark; }
    public Map<Integer, Long> getMarkDistribution() { return markDistribution; }
}
