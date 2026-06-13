package com.eduapp.backend.controller;

import com.eduapp.backend.dto.SchoolEnrolmentDto;
import com.eduapp.backend.repository.SchoolEnrolmentRepository;
import com.eduapp.backend.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class roster for teachers/admins. Tenant-scoped: only enrolments belonging to the
 * caller's school are returned (queried with the class id AND the current tenant id).
 */
@RestController
@RequestMapping("/api/school/classes/{classId}/roster")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
public class SchoolRosterController {

    private final SchoolEnrolmentRepository enrolmentRepository;

    public SchoolRosterController(SchoolEnrolmentRepository enrolmentRepository) {
        this.enrolmentRepository = enrolmentRepository;
    }

    @GetMapping
    public ResponseEntity<List<SchoolEnrolmentDto>> roster(@PathVariable Long classId) {
        List<SchoolEnrolmentDto> roster = enrolmentRepository
                .findByClassIdAndSchoolId(classId, TenantContext.getSchoolId()).stream()
                .map(SchoolEnrolmentDto::from)
                .toList();
        return ResponseEntity.ok(roster);
    }
}
