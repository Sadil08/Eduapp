package com.eduapp.backend.repository;

import com.eduapp.backend.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Tenant-scoped repository. Callers MUST use the {@code ...BySchoolId} methods so every
 * query is filtered by the current tenant. (ArchUnit rule in WP-4.4 forbids raw
 * {@code findAll()} on tenant-scoped repositories.)
 */
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findBySchoolId(Long schoolId);

    Optional<SchoolClass> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<SchoolClass> findByClassCode(String classCode);

    boolean existsByClassCode(String classCode);
}
