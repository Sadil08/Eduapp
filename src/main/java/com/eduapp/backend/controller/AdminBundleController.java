package com.eduapp.backend.controller;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.AdminBundleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for admin bundle management.
 * All endpoints require ADMIN role.
 * Provides CRUD operations and statistics for paper bundles.
 */
@RestController
@RequestMapping("/api/admin/bundles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBundleController {

    private static final Logger logger = LoggerFactory.getLogger(AdminBundleController.class);

    private final AdminBundleService adminBundleService;
    private final JwtUtil jwtUtil;

    public AdminBundleController(AdminBundleService adminBundleService, JwtUtil jwtUtil) {
        this.adminBundleService = adminBundleService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get system-wide statistics
     * GET /api/admin/bundles/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<SystemStatsDto> getSystemStats() {
        logger.info("Admin requested system statistics");
        SystemStatsDto stats = adminBundleService.getSystemStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all bundles with statistics
     * GET /api/admin/bundles
     */
    @GetMapping
    public ResponseEntity<List<AdminBundleDto>> getAllBundles() {
        logger.info("Admin requested all bundles with stats");
        List<AdminBundleDto> bundles = adminBundleService.getAllBundlesWithStats();
        return ResponseEntity.ok(bundles);
    }

    /**
     * Get specific bundle statistics
     * GET /api/admin/bundles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BundleStatsDto> getBundleStats(@PathVariable Long id) {
        logger.info("Admin requested stats for bundle ID: {}", id);
        try {
            BundleStatsDto stats = adminBundleService.getBundleStats(id);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            logger.warn("Bundle not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new bundle
     * POST /api/admin/bundles
     */
    @PostMapping
    public ResponseEntity<PaperBundle> createBundle(
            @RequestBody PaperBundleDto dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        Long adminId = jwtUtil.extractUserId(token);

        logger.info("Admin {} creating bundle: {}", adminId, dto.getName());

        try {
            PaperBundle created = adminBundleService.createBundle(dto, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating bundle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing bundle
     * PUT /api/admin/bundles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PaperBundle> updateBundle(
            @PathVariable Long id,
            @RequestBody PaperBundleDto dto) {

        logger.info("Admin updating bundle ID: {}", id);

        try {
            PaperBundle updated = adminBundleService.updateBundle(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Bundle not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating bundle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a bundle
     * DELETE /api/admin/bundles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBundle(@PathVariable Long id) {
        logger.info("Admin deleting bundle ID: {}", id);

        try {
            adminBundleService.deleteBundle(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Bundle not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting bundle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add a paper to a bundle
     * POST /api/admin/bundles/{bundleId}/papers/{paperId}
     */
    @PostMapping("/{bundleId}/papers/{paperId}")
    public ResponseEntity<Void> addPaperToBundle(
            @PathVariable Long bundleId,
            @PathVariable Long paperId) {

        logger.info("Admin adding paper {} to bundle {}", paperId, bundleId);

        try {
            adminBundleService.addPaperToBundle(bundleId, paperId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Bundle or paper not found: bundle={}, paper={}", bundleId, paperId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding paper to bundle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Remove a paper from a bundle
     * DELETE /api/admin/bundles/{bundleId}/papers/{paperId}
     */
    @DeleteMapping("/{bundleId}/papers/{paperId}")
    public ResponseEntity<Void> removePaperFromBundle(
            @PathVariable Long bundleId,
            @PathVariable Long paperId) {

        logger.info("Admin removing paper {} from bundle {}", paperId, bundleId);

        try {
            adminBundleService.removePaperFromBundle(bundleId, paperId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Paper not in bundle: bundle={}, paper={}", bundleId, paperId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error removing paper from bundle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
