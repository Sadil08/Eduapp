package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {}
