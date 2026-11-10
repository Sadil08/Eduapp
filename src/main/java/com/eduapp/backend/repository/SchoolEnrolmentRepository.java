package com.eduapp.backend.repository;

import com.eduapp.backend.model.SchoolEnrolment;
import com.eduapp.backend.model.SchoolEnrolmentStatus;
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

    /** Owner-scoped: a student's own enrolments across any school (e.g. PENDING before a school bind). */
    List<SchoolEnrolment> findByStudentId(Long studentId);

    boolean existsByClassIdAndStudentId(Long classId, Long studentId);

    // BT-9: approval-aware queries.
    List<SchoolEnrolment> findByClassIdAndSchoolIdAndStatus(Long classId, Long schoolId, SchoolEnrolmentStatus status);

    boolean existsByClassIdAndStudentIdAndStatus(Long classId, Long studentId, SchoolEnrolmentStatus status);

    /** A student's enrolments in a given status within their school (for the student dashboard). */
    List<SchoolEnrolment> findByStudentIdAndSchoolIdAndStatus(Long studentId, Long schoolId, SchoolEnrolmentStatus status);
}
