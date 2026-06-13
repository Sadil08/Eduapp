package com.eduapp.backend.repository;

import com.eduapp.backend.model.StudentConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentConsentRecordRepository extends JpaRepository<StudentConsentRecord, Long> {

    Optional<StudentConsentRecord> findByUserId(Long userId);
}
