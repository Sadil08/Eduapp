package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eduapp.backend.model.AIAnalysis;
import java.util.List;

public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {
    @Query("SELECT a FROM AIAnalysis a WHERE a.answer.attempt.student.id = :studentId")
    List<AIAnalysis> findByAnswerAttemptStudentId(@Param("studentId") Long studentId);
}