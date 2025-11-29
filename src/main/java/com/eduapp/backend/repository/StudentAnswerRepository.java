package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.StudentAnswer;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
}