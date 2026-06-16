package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.dto.UserResponse;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.security.RoleCreationPolicy;
import com.eduapp.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')") // 👈 restricts to admin users
    public ResponseEntity<UserResponse> createAdmin(@AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody RegisterRequest req) {
        // SEC-4: enforce the role-creation matrix (defence in depth behind @PreAuthorize).
        RoleCreationPolicy.assertCanCreate(principal.getRole(), Role.ADMIN);
        // SECURITY: return a DTO, never the raw User entity (would expose the BCrypt hash).
        User created = userService.createAdmin(req);
        return ResponseEntity.ok(
                new UserResponse(created.getId(), created.getUsername(), created.getEmail(), created.getRole()));
    }
}
