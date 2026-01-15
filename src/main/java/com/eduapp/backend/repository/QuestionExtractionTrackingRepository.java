package com.eduapp.backend.repository;

import com.eduapp.backend.model.QuestionExtractionTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for QuestionExtractionTracking entity.
 * Manages extraction limit tracking per question per student attempt.
 */
@Repository
public interface QuestionExtractionTrackingRepository extends JpaRepository<QuestionExtractionTracking, Long> {

    /**
     * Find tracking record for a specific attempt and question
     * 
     * @param attemptId  Student paper attempt ID
     * @param questionId Question ID
     * @return Optional tracking record
     */
    @Query("SELECT qet FROM QuestionExtractionTracking qet " +
            "WHERE qet.studentPaperAttempt.id = :attemptId " +
            "AND qet.question.id = :questionId")
    Optional<QuestionExtractionTracking> findByAttemptIdAndQuestionId(
            @Param("attemptId") Long attemptId,
            @Param("questionId") Long questionId);

    /**
     * Find all tracking records for a specific attempt
     * Useful for displaying extraction counts for all questions
     * 
     * @param attemptId Student paper attempt ID
     * @return List of tracking records
     */
    @Query("SELECT qet FROM QuestionExtractionTracking qet " +
            "WHERE qet.studentPaperAttempt.id = :attemptId")
    List<QuestionExtractionTracking> findByAttemptId(@Param("attemptId") Long attemptId);

    /**
     * Count total extractions across all attempts for analytics
     * 
     * @return Total extraction count
     */
    @Query("SELECT COALESCE(SUM(qet.extractionCount), 0) FROM QuestionExtractionTracking qet")
    Long sumAllExtractionCounts();

    /**
     * Count total extractions for a specific user for analytics
     * 
     * @param userId User ID
     * @return Total extraction count for user
     */
    @Query("SELECT COALESCE(SUM(qet.extractionCount), 0) FROM QuestionExtractionTracking qet " +
            "WHERE qet.studentPaperAttempt.student.id = :userId")
    Long sumExtractionCountsByUserId(@Param("userId") Long userId);

    /**
     * Delete all tracking records for a specific attempt
     * Used when an attempt is deleted (cascade should handle this, but explicit
     * method for clarity)
     * 
     * @param attemptId Student paper attempt ID
     */
    void deleteByStudentPaperAttemptId(Long attemptId);

    // Additional analytics queries
    @Query("SELECT COALESCE(SUM(qet.extractionCount), 0) FROM QuestionExtractionTracking qet " +
           "WHERE qet.lastExtractionTime >= :since")
    Long sumTodayExtractionCounts(@Param("since") LocalDateTime since);

    @Query("SELECT qet.studentPaperAttempt.student.id, SUM(qet.extractionCount) " +
           "FROM QuestionExtractionTracking qet " +
           "GROUP BY qet.studentPaperAttempt.student.id")
    List<Object[]> sumExtractionCountsGroupedByUser();

    @Query("SELECT u.country, SUM(qet.extractionCount) " +
           "FROM QuestionExtractionTracking qet " +
           "JOIN qet.studentPaperAttempt.student u " +
           "WHERE u.country IS NOT NULL " +
           "GROUP BY u.country")
    List<Object[]> sumExtractionCountsByCountry();

    @Query("SELECT COUNT(DISTINCT qet.studentPaperAttempt.student.id) " +
           "FROM QuestionExtractionTracking qet " +
           "WHERE qet.extractionCount >= qet.maxExtractions")
    Long countUsersHitLimit();
}

