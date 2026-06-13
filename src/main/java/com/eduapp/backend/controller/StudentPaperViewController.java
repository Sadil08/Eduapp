package com.eduapp.backend.controller;

import com.eduapp.backend.dto.SchoolPaperDto;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.SchoolEnrolmentRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.SchoolPaperService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Student-facing view of school papers (WP-6). Outside /api/school/** (which is
 * teacher/admin only). A student only ever sees ASSIGNED papers, and only for a class
 * they are enrolled in — draft/approved papers are invisible.
 */
@RestController
@RequestMapping("/api/student/papers")
@PreAuthorize("hasRole('SCHOOL_STUDENT')")
public class StudentPaperViewController {

    private final SchoolPaperService paperService;
    private final SchoolEnrolmentRepository enrolmentRepository;
    private final UserRepository userRepository;

    public StudentPaperViewController(SchoolPaperService paperService,
            SchoolEnrolmentRepository enrolmentRepository,
            UserRepository userRepository) {
        this.paperService = paperService;
        this.enrolmentRepository = enrolmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<SchoolPaperDto>> assignedForClass(@AuthenticationPrincipal UserDetails principal,
            @RequestParam Long classId) {
        User student = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!enrolmentRepository.existsByClassIdAndStudentId(classId, student.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not enrolled in this class");
        }
        return ResponseEntity.ok(paperService.listAssignedForClass(classId).stream()
                .map(SchoolPaperDto::from).toList());
    }
}
