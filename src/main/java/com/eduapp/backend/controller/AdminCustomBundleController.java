package com.eduapp.backend.controller;

import com.eduapp.backend.dto.CustomBundleDto;
import com.eduapp.backend.model.CustomBundleStatus;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.CustomBundleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

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

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * SCALE-4: paginated approval queue. GET /api/admin/custom-bundles/pending?page=&size=
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<CustomBundleDto>> getPendingApproval(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        return ResponseEntity.ok(customBundleService.getPendingApproval(pageable));
    }

    /**
     * SCALE-4: paginated approved listing. GET /api/admin/custom-bundles/approved?page=&size=
     */
    @GetMapping("/approved")
    public ResponseEntity<Page<CustomBundleDto>> getApprovedBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        return ResponseEntity.ok(customBundleService.getApprovedBundles(pageable));
    }

    /**
     * Cheap COUNT totals for the admin custom-bundle queues.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
                "pending", customBundleService.countByStatus(CustomBundleStatus.PURCHASED),
                "approved", customBundleService.countByStatus(CustomBundleStatus.APPROVED)));
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
