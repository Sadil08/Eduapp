package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.StudentPaperAttempt;

public interface StudentPaperAttemptRepository extends JpaRepository<StudentPaperAttempt, Long> {
}