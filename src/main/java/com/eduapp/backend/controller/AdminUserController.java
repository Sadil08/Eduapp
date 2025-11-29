package com.eduapp.backend.controller;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for admin user management.
 * All endpoints require ADMIN role.
 * Provides user listing, bundle access control, and attempt limit management.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final AdminUserService adminUserService;
    private final JwtUtil jwtUtil;

    public AdminUserController(AdminUserService adminUserService, JwtUtil jwtUtil) {
        this.adminUserService = adminUserService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get all users with statistics
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        logger.info("Admin requested all users");
        List<AdminUserDto> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user details
     * GET /api/admin/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDto> getUserDetails(@PathVariable Long id) {
        logger.info("Admin requested user details for ID: {}", id);
        try {
            AdminUserDto user = adminUserService.getUserDetails(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            logger.warn("User not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get user's bundle access list
     * GET /api/admin/users/{id}/bundles
     */
    @GetMapping("/{id}/bundles")
    public ResponseEntity<List<UserBundleAccessDto>> getUserBundles(@PathVariable Long id) {
        logger.info("Admin requested bundle access for user ID: {}", id);
        List<UserBundleAccessDto> bundles = adminUserService.getUserBundleAccess(id);
        return ResponseEntity.ok(bundles);
    }

    /**
     * Grant bundle access to a user
     * POST /api/admin/users/{userId}/bundles
     */
    @PostMapping("/{userId}/bundles")
    public ResponseEntity<StudentBundleAccess> grantBundleAccess(
            @PathVariable Long userId,
            @RequestBody GrantBundleAccessDto dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        Long adminId = jwtUtil.extractUserId(token);

        logger.info("Admin {} granting bundle {} to user {}", adminId, dto.getBundleId(), userId);

        try {
            StudentBundleAccess access = adminUserService.grantBundleAccess(
                    userId, dto.getBundleId(), adminId, dto.getReason());
            return ResponseEntity.status(HttpStatus.CREATED).body(access);
        } catch (IllegalArgumentException e) {
            logger.warn("Error granting access: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error granting bundle access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Revoke bundle access from a user
     * DELETE /api/admin/users/{userId}/bundles/{bundleId}
     */
    @DeleteMapping("/{userId}/bundles/{bundleId}")
    public ResponseEntity<Void> revokeBundleAccess(
            @PathVariable Long userId,
            @PathVariable Long bundleId) {

        logger.info("Admin revoking bundle {} from user {}", bundleId, userId);

        try {
            adminUserService.revokeBundleAccess(userId, bundleId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Error revoking access: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error revoking bundle access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user's attempt information
     * GET /api/admin/users/{id}/attempts
     */
    @GetMapping("/{id}/attempts")
    public ResponseEntity<List<UserAttemptInfoDto>> getUserAttempts(@PathVariable Long id) {
        logger.info("Admin requested attempt info for user ID: {}", id);
        List<UserAttemptInfoDto> attempts = adminUserService.getUserAttempts(id);
        return ResponseEntity.ok(attempts);
    }

    /**
     * Update attempt limit for a user on a specific paper
     * PUT /api/admin/users/{userId}/papers/{paperId}/attempts
     */
    @PutMapping("/{userId}/papers/{paperId}/attempts")
    public ResponseEntity<Void> updateAttemptLimit(
            @PathVariable Long userId,
            @PathVariable Long paperId,
            @RequestBody UpdateAttemptLimitDto dto) {

        logger.info("Admin updating attempt limit for user {} on paper {} to {}",
                userId, paperId, dto.getMaxFreeAttempts());

        try {
            adminUserService.updateAttemptLimit(userId, paperId, dto.getMaxFreeAttempts());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Error updating attempt limit: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating attempt limit", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
