package com.eduapp.backend.service;

import com.eduapp.backend.dto.GlobalScoreAggregateDto;
import com.eduapp.backend.repository.OverallPaperAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cambridge-shareable aggregate analytics (PRODUCT_BUSINESS_PLAN.md §7, §10.4).
 *
 * Two privacy guarantees are enforced here and proven by tests:
 * <ul>
 *   <li><b>Consent + tier filter</b>: the source query only includes GLOBAL-tier students
 *       (school IS NULL — school data is never exported) who consented to analytics sharing.</li>
 *   <li><b>Minimum cohort floor</b>: never emit a statistic for fewer than {@value #MINIMUM_COHORT_SIZE}
 *       consenting students (standard k-anonymity threshold to prevent re-identification).</li>
 * </ul>
 * Only aggregate numbers leave this service — never identities or raw rows.
 */
@Service
public class GlobalAnalyticsService {

    /** Standard anonymisation threshold — do not lower without a privacy review. */
    public static final int MINIMUM_COHORT_SIZE = 50;

    private final OverallPaperAnalysisRepository overallPaperAnalysisRepository;

    public GlobalAnalyticsService(OverallPaperAnalysisRepository overallPaperAnalysisRepository) {
        this.overallPaperAnalysisRepository = overallPaperAnalysisRepository;
    }

    @Transactional(readOnly = true)
    public GlobalScoreAggregateDto paperScoreAggregate(Long paperId) {
        List<Integer> scores = overallPaperAnalysisRepository.findConsentingGlobalScoresForPaper(paperId);

        if (scores.size() < MINIMUM_COHORT_SIZE) {
            // Suppress entirely — emitting stats for a tiny cohort risks re-identification.
            return GlobalScoreAggregateDto.suppressed(paperId, MINIMUM_COHORT_SIZE);
        }

        double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
        int min = scores.stream().mapToInt(Integer::intValue).min().orElse(0);
        int max = scores.stream().mapToInt(Integer::intValue).max().orElse(0);
        return GlobalScoreAggregateDto.of(paperId, MINIMUM_COHORT_SIZE, scores.size(), avg, min, max);
    }
}
