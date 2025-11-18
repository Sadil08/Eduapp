package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.StudentBundleAccess;

public interface StudentBundleAccessRepository extends JpaRepository<StudentBundleAccess, Long> {}