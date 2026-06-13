package com.eduapp.backend.controller;

import com.eduapp.backend.dto.CustomBundleDto;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.CustomBundleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin controller for managing custom bundles.
 */
@RestController
@RequestMapping("/api/admin/custom-bundles")
@PreAuthorize("hasRole('ADMIN')") // SECURITY: method-level guard in addition to URL rule (defense-in-depth)
public class AdminCustomBundleController {

    private final CustomBundleService customBundleService;
    private final JwtUtil jwtUtil;

    public AdminCustomBundleController(CustomBundleService customBundleService, JwtUtil jwtUtil) {
        this.customBundleService = customBundleService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get all bundles pending approval.
     * GET /api/admin/custom-bundles/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<CustomBundleDto>> getPendingApproval() {
        List<CustomBundleDto> bundles = customBundleService.getPendingApproval();
        return ResponseEntity.ok(bundles);
    }

    /**
     * Get all approved bundles.
     * GET /api/admin/custom-bundles/approved
     */
    @GetMapping("/approved")
    public ResponseEntity<List<CustomBundleDto>> getApprovedBundles() {
        List<CustomBundleDto> bundles = customBundleService.getApprovedBundles();
        return ResponseEntity.ok(bundles);
    }

    /**
     * Approve a custom bundle.
     * POST /api/admin/custom-bundles/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<CustomBundleDto> approveBundle(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        Long adminId = extractUserId(authHeader);
        CustomBundleDto approved = customBundleService.approveBundle(id, adminId);
        return ResponseEntity.ok(approved);
    }

    /**
     * Update price per paper.
     * PUT /api/admin/custom-bundles/price-per-paper
     */
    @PutMapping("/price-per-paper")
    public ResponseEntity<Void> updatePricePerPaper(@RequestBody UpdatePriceRequest request) {
        customBundleService.updatePricePerPaper(request.getPrice());
        return ResponseEntity.ok().build();
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    // DTOs
    public static class UpdatePriceRequest {
        private BigDecimal price;

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
