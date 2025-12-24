package com.eduapp.backend.repository;

import com.eduapp.backend.model.QuestionModelAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionModelAnswerRepository extends JpaRepository<QuestionModelAnswer, Long> {
    
    /**
     * Find all model answers for a specific question
     */
    List<QuestionModelAnswer> findByQuestionId(Long questionId);
    
    /**
     * Find the most recent model answer for a question
     */
    Optional<QuestionModelAnswer> findFirstByQuestionIdOrderByCreatedAtDesc(Long questionId);
    
    /**
     * Delete all model answers for a question
     */
    void deleteByQuestionId(Long questionId);
}
