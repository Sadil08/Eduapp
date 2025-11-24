package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.StudentPaperAttempt;
import java.util.List;

public interface StudentPaperAttemptRepository extends JpaRepository<StudentPaperAttempt, Long> {
    List<StudentPaperAttempt> findByStudentId(Long studentId);
}