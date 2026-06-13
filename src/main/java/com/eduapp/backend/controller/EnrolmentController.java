package com.eduapp.backend.controller;

import com.eduapp.backend.dto.JoinClassRequest;
import com.eduapp.backend.dto.SchoolEnrolmentDto;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.EnrolmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Student self-enrolment by class code (WP-5). Any authenticated user may join; joining
 * binds them to the class's school and upgrades STUDENT -> SCHOOL_STUDENT.
 * Lives outside /api/school/** so plain students (not yet school members) can reach it.
 */
@RestController
@RequestMapping("/api/enrolments")
public class EnrolmentController {

    private final EnrolmentService enrolmentService;
    private final UserRepository userRepository;

    public EnrolmentController(EnrolmentService enrolmentService, UserRepository userRepository) {
        this.enrolmentService = enrolmentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/join")
    public ResponseEntity<SchoolEnrolmentDto> join(@AuthenticationPrincipal UserDetails principal,
            @RequestBody JoinClassRequest req) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(SchoolEnrolmentDto.from(
                enrolmentService.joinByClassCode(user.getId(), req.getClassCode())));
    }
}
