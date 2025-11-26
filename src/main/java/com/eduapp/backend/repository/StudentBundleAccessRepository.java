package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.StudentBundleAccess;
import java.util.List;

public interface StudentBundleAccessRepository extends JpaRepository<StudentBundleAccess, Long> {
    List<StudentBundleAccess> findByStudentId(Long studentId);
    boolean existsByStudentIdAndBundleId(Long studentId, Long bundleId);
}