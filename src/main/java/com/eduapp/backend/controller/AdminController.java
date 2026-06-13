package com.eduapp.backend.controller;

import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.dto.UserResponse;
import com.eduapp.backend.model.User;
import com.eduapp.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')") // 👈 restricts to admin users
    public ResponseEntity<UserResponse> createAdmin(@RequestBody RegisterRequest req) {
        // SECURITY: return a DTO, never the raw User entity (would expose the BCrypt hash).
        User created = userService.createAdmin(req);
        return ResponseEntity.ok(
                new UserResponse(created.getId(), created.getUsername(), created.getEmail(), created.getRole()));
    }
}
