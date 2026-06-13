package com.eduapp.backend.repository;

import com.eduapp.backend.model.SchoolPaperAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Tenant-scoped (ArchUnit forbids raw {@code findAll()}). */
@TenantScoped
public interface SchoolPaperAttemptRepository extends JpaRepository<SchoolPaperAttempt, Long> {

    Optional<SchoolPaperAttempt> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<SchoolPaperAttempt> findBySchoolPaperIdAndStudentId(Long schoolPaperId, Long studentId);

    List<SchoolPaperAttempt> findBySchoolPaperIdAndSchoolId(Long schoolPaperId, Long schoolId);
}
