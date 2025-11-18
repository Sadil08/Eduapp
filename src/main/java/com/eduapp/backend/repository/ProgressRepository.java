package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Progress;

public interface ProgressRepository extends JpaRepository<Progress, Long> {}