package com.eduapp.backend.controller;

import com.eduapp.backend.dto.SchoolClassDto;
import com.eduapp.backend.model.SchoolEnrolmentStatus;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.SchoolClassRepository;
import com.eduapp.backend.repository.SchoolEnrolmentRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The student's own ACTIVE classes — used by the student dashboard to navigate to assigned
 * papers. Pending/rejected enrolments are excluded (BT-9).
 */
@RestController
@RequestMapping("/api/student/classes")
@PreAuthorize("hasRole('SCHOOL_STUDENT')")
public class StudentClassController {

    private final SchoolEnrolmentRepository enrolmentRepository;
    private final SchoolClassRepository classRepository;
    private final UserRepository userRepository;

    public StudentClassController(SchoolEnrolmentRepository enrolmentRepository,
            SchoolClassRepository classRepository, UserRepository userRepository) {
        this.enrolmentRepository = enrolmentRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<SchoolClassDto>> myClasses(@AuthenticationPrincipal UserDetails principal) {
        User student = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Long schoolId = TenantContext.getSchoolId();
        List<SchoolClassDto> classes = enrolmentRepository
                .findByStudentIdAndSchoolIdAndStatus(student.getId(), schoolId, SchoolEnrolmentStatus.ACTIVE)
                .stream()
                .map(e -> classRepository.findByIdAndSchoolId(e.getClassId(), schoolId).orElse(null))
                .filter(c -> c != null)
                .map(SchoolClassDto::from)
                .toList();
        return ResponseEntity.ok(classes);
    }
}
