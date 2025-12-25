package com.eduapp.backend.repository;

import com.eduapp.backend.model.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExamTypeRepository extends JpaRepository<ExamType, Long> {
    Optional<ExamType> findByName(String name);

    boolean existsByName(String name);
}
