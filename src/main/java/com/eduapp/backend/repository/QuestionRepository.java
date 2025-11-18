package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}