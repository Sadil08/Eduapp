package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.PaperBundle;

public interface PaperBundleRepository extends JpaRepository<PaperBundle, Long> {
}