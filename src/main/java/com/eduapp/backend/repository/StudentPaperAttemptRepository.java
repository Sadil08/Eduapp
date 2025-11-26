package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
     * Count all attempts for a specific paper (for admin statistics).
     */
    long countByPaperId(Long paperId);

    /**
     * Count all attempts for a specific student (for admin statistics).
     */
    long countByStudentId(Long studentId);

    /**
     * Calculate average score for a paper (for admin statistics).
     * Note: This would typically be a custom @Query method.
     * Returning null for now - implement with custom query if needed.
     */
    default Double findAverageScoreByPaperId(Long paperId) {
        // TODO: Implement with @Query to calculate average of totalMarks
        return null;
    }
}