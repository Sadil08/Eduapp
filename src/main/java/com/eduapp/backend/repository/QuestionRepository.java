package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // School-tier questions (those carrying a school_paper_id).
    List<Question> findBySchoolPaperId(Long schoolPaperId);

    long countBySchoolPaperId(Long schoolPaperId);
}