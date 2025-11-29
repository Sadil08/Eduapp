package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.OverallPaperAnalysis;
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
}