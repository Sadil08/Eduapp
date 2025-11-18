package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.QuestionOption;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
}