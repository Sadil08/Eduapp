package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eduapp.backend.model.StudentPaperAttempt;
import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentPaperAttempt entities.
 * Provides database access for paper attempt records.
 */
public interface StudentPaperAttemptRepository extends JpaRepository<StudentPaperAttempt, Long> {

    /**
     * Finds all attempts by a specific student.
     * Used for student dashboard and history.
     */
    List<StudentPaperAttempt> findByStudentId(Long studentId);

    /**
     * Finds attempts for a specific paper where student opted into leaderboard.
     * Used for leaderboard generation, ordered by time taken (fastest first).
     */
    List<StudentPaperAttempt> findByPaperIdAndOptedInTrueOrderByTimeTakenMinutesAsc(Long paperId);

    /**
     * Counts the number of attempts a student has made for a specific paper.
     * Used to calculate the next attempt number.
     */
    int countByStudentIdAndPaperId(Long studentId, Long paperId);

    /**
     * Finds an attempt by ID, but only if it belongs to the specified student.
     * Used for security - ensures students can only access their own attempts.
     */
    Optional<StudentPaperAttempt> findByIdAndStudentId(Long id, Long studentId);

    /**
     * Finds an attempt by ID with all answers, questions, and options eagerly
     * loaded.
     * Used for AI analysis to ensure all required data is available.
     */
    @EntityGraph(attributePaths = { "answers", "answers.question", "answers.selectedOption", "paper" })
    @Query("SELECT a FROM StudentPaperAttempt a WHERE a.id = :id")
    Optional<StudentPaperAttempt> findByIdWithAnswers(@Param("id") Long id);

    /**
     * Count all attempts for a specific paper (for admin statistics).
     */
    long countByPaperId(Long paperId);

    /**
     * Count all attempts for a specific student (for admin statistics).
     */
    long countByStudentId(Long studentId);

    /**
     * Finds all attempts by a specific student for a specific paper, ordered by
     * start time (newest first).
     * Used for viewing attempt history.
     */
    List<StudentPaperAttempt> findByStudentIdAndPaperIdOrderByStartedAtDesc(Long studentId, Long paperId);
    
    /**
     * Finds all attempts by a specific student for a specific paper in a specific bundle context.
     * Used for viewing attempt history scoped by bundle.
     */
    List<StudentPaperAttempt> findByStudentIdAndPaperIdAndOriginBundleIdOrderByStartedAtDesc(Long studentId, Long paperId, Long originBundleId);
    
    /**
     * Counts the number of attempts a student has made for a specific paper in a specific bundle context.
     */
    @Query("SELECT COUNT(a) FROM StudentPaperAttempt a WHERE a.student.id = :studentId AND a.paper.id = :paperId AND a.originBundle.id = :originBundleId")
    int countByStudentIdAndPaperIdAndOriginBundleId(@Param("studentId") Long studentId, @Param("paperId") Long paperId, @Param("originBundleId") Long originBundleId);

    /**
     * Counts all attempts associated with a specific origin bundle.
     * Used for bundle-level statistics.
     */
    long countByOriginBundleId(Long originBundleId);
    
    /**
     * Find specific attempts by status (original method without bundle scoping).
     * Returns a list in case of duplicates (should strictly be one, but we handle potential data inconsistency).
     */
    List<StudentPaperAttempt> findByStudentIdAndPaperIdAndStatusOrderByStartedAtDesc(Long studentId, Long paperId, com.eduapp.backend.model.AttemptStatus status);
    
    /**
     * Find specific attempts by status in a specific bundle context.
     * Returns a list in case of duplicates.
     */
    List<StudentPaperAttempt> findByStudentIdAndPaperIdAndOriginBundleIdAndStatusOrderByStartedAtDesc(Long studentId, Long paperId, Long originBundleId, com.eduapp.backend.model.AttemptStatus status);

    /**
     * Calculate average score for a paper (for admin statistics).
     * Note: This would typically be a custom @Query method.
     * Returning null for now - implement with custom query if needed.
     */
    default Double findAverageScoreByPaperId(Long paperId) {
        // TODO: Implement with @Query to calculate average of totalMarks
        return null;
    }

    /**
     * Finds all attempts by a specific student for a specific paper in a specific custom bundle context.
     * Used for viewing attempt history scoped by custom bundle.
     */
    List<StudentPaperAttempt> findByStudentIdAndPaperIdAndOriginCustomBundleIdOrderByStartedAtDesc(Long studentId, Long paperId, Long originCustomBundleId);

    /**
     * Counts the number of attempts a student has made for a specific paper in a specific custom bundle context.
     */
    @Query("SELECT COUNT(a) FROM StudentPaperAttempt a WHERE a.student.id = :studentId AND a.paper.id = :paperId AND a.originCustomBundle.id = :originCustomBundleId")
    int countByStudentIdAndPaperIdAndOriginCustomBundleId(@Param("studentId") Long studentId, @Param("paperId") Long paperId, @Param("originCustomBundleId") Long originCustomBundleId);

    /**
     * Find specific attempts by status in a specific custom bundle context.
     * Returns a list in case of duplicates.
     */
    List<StudentPaperAttempt> findByStudentIdAndPaperIdAndOriginCustomBundleIdAndStatusOrderByStartedAtDesc(Long studentId, Long paperId, Long originCustomBundleId, com.eduapp.backend.model.AttemptStatus status);

    // Analytics queries
    @Query("SELECT spa.student.id, COUNT(spa) FROM StudentPaperAttempt spa " +
           "GROUP BY spa.student.id")
    List<Object[]> countAttemptsByUser();
}