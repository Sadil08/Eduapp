package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Paper;

public interface PaperRepository extends JpaRepository<Paper, Long> {
}