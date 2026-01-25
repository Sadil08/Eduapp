package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eduapp.backend.model.StudentAnswer;

import java.util.List;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByAttemptId(Long attemptId);

    /**
     * Find a specific answer for an attempt and question
     */
    java.util.Optional<StudentAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    /**
     * Find all draft answers for a specific attempt
     */
    List<StudentAnswer> findByAttemptIdAndIsDraft(Long attemptId, Boolean isDraft);

    /**
     * Delete all draft answers for a specific attempt
     */
    @Modifying
    @Query("DELETE FROM StudentAnswer sa WHERE sa.attempt.id = :attemptId AND sa.isDraft = :isDraft")
    void deleteByAttemptIdAndIsDraft(@Param("attemptId") Long attemptId, @Param("isDraft") Boolean isDraft);

    /**
     * Count draft answers for an attempt
     */
    long countByAttemptIdAndIsDraft(Long attemptId, Boolean isDraft);
}