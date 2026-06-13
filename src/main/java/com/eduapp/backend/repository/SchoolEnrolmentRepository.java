package com.eduapp.backend.repository;

import com.eduapp.backend.model.SchoolEnrolment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Tenant-scoped: query only via the {@code ...BySchoolId} / scoped methods. Raw
 * {@code findAll()} is forbidden by the ArchUnit rule in {@code TenantArchitectureTest}.
 */
@TenantScoped
public interface SchoolEnrolmentRepository extends JpaRepository<SchoolEnrolment, Long> {

    List<SchoolEnrolment> findBySchoolId(Long schoolId);

    List<SchoolEnrolment> findByClassIdAndSchoolId(Long classId, Long schoolId);

    Optional<SchoolEnrolment> findByClassIdAndStudentId(Long classId, Long studentId);

    boolean existsByClassIdAndStudentId(Long classId, Long studentId);
}
