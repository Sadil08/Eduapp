package com.eduapp.backend.controller;

import com.eduapp.backend.dto.UserResponse;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** BT-5.1: the school's teachers, for the SCHOOL_ADMIN to pick when assigning to classes. */
@RestController
@RequestMapping("/api/school/teachers")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
public class SchoolTeacherController {

    private final UserRepository userRepository;

    public SchoolTeacherController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> teachers() {
        Long schoolId = TenantContext.getSchoolId();
        List<UserResponse> teachers = userRepository.findBySchool_IdAndRole(schoolId, Role.TEACHER).stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole()))
                .toList();
        return ResponseEntity.ok(teachers);
    }
}
