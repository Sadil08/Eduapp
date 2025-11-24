package com.eduapp.backend.controller;

import com.eduapp.backend.dto.UserDetailDto;
import com.eduapp.backend.dto.UserResponse;
import com.eduapp.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for admin user management.
 * Provides endpoints for viewing all users and detailed user information.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Handles GET request to retrieve all users, requires admin role
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Admin requested all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Handles GET request to retrieve detailed information for a specific user, requires admin role
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailDto> getUserDetails(@PathVariable Long id) {
        logger.info("Admin requested detailed data for user ID: {}", id);
        UserDetailDto userDetail = userService.getUserDetails(id);
        return ResponseEntity.ok(userDetail);
    }
}