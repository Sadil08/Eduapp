package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    long countBySubjectId(Long subjectId);
}
