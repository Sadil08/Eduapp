package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.StudentBundleAccess;
import java.util.List;

public interface StudentBundleAccessRepository extends JpaRepository<StudentBundleAccess, Long> {
    /**
     * Find all bundle accesses for a specific student
     */
    List<StudentBundleAccess> findByStudentId(Long studentId);

    /**
     * Check if student has access to a bundle
     */
    boolean existsByStudentIdAndBundleId(Long studentId, Long bundleId);

    /**
     * Count number of students with access to a specific bundle
     */
    int countByBundleId(Long bundleId);

    /**
     * Find access record by student and bundle for admin grant/revoke
     */
    StudentBundleAccess findByStudentIdAndBundleId(Long studentId, Long bundleId);
}