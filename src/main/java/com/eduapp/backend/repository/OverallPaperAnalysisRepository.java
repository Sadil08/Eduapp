package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eduapp.backend.model.OverallPaperAnalysis;

import java.util.List;
import java.util.Optional;

/**
 * Repository for OverallPaperAnalysis entities.
 * Provides database access for AI-generated overall paper analysis.
 */
public interface OverallPaperAnalysisRepository extends JpaRepository<OverallPaperAnalysis, Long> {

    /**
     * Finds the overall analysis for a specific attempt.
     * Used to retrieve AI feedback and total marks for an attempt.
     */
    Optional<OverallPaperAnalysis> findByAttemptId(Long attemptId);

    /**
     * Cambridge-shareable source (WP-9): scores for a paper from GLOBAL-tier students
     * ({@code school IS NULL} — never school-tier data) who consented to analytics sharing.
     * Returns raw scores only for in-memory aggregation; callers MUST apply the minimum
     * cohort-size floor before exposing anything.
     */
    @Query("SELECT o.totalMarks FROM OverallPaperAnalysis o "
            + "WHERE o.attempt.paper.id = :paperId "
            + "AND o.attempt.student.school IS NULL "
            + "AND o.totalMarks IS NOT NULL "
            + "AND o.attempt.student.id IN "
            + "(SELECT c.userId FROM StudentConsentRecord c WHERE c.analyticsSharingConsented = true)")
    List<Integer> findConsentingGlobalScoresForPaper(@Param("paperId") Long paperId);
}