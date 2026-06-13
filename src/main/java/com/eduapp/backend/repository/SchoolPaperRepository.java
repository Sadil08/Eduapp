package com.eduapp.backend.repository;

import com.eduapp.backend.model.SchoolPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Tenant-scoped: query only via the scoped methods (ArchUnit forbids raw {@code findAll()}).
 */
@TenantScoped
public interface SchoolPaperRepository extends JpaRepository<SchoolPaper, Long> {

    List<SchoolPaper> findBySchoolId(Long schoolId);

    Optional<SchoolPaper> findByIdAndSchoolId(Long id, Long schoolId);

    List<SchoolPaper> findByClassIdAndSchoolId(Long classId, Long schoolId);
}
