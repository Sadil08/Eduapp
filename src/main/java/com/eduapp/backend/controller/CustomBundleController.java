package com.eduapp.backend.controller;

import com.eduapp.backend.dto.CustomBundleDto;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.CustomBundleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for student custom bundle operations.
 */
@RestController
@RequestMapping("/api/custom-bundles")
public class CustomBundleController {

    private final CustomBundleService customBundleService;
    private final JwtUtil jwtUtil;

    public CustomBundleController(CustomBundleService customBundleService, JwtUtil jwtUtil) {
        this.customBundleService = customBundleService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get user's current draft bundle.
     * GET /api/custom-bundles/my-draft
     */
    @GetMapping("/my-draft")
    public ResponseEntity<CustomBundleDto> getMyDraft(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        CustomBundleDto draft = customBundleService.getMyDraft(userId);

        if (draft == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(draft);
    }

    /**
     * Get all bundles owned by the user (purchased or approved).
     * GET /api/custom-bundles/my-bundles
     */
    @GetMapping("/my-bundles")
    public ResponseEntity<List<CustomBundleDto>> getMyBundles(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        List<CustomBundleDto> bundles = customBundleService.getMyBundles(userId);
        return ResponseEntity.ok(bundles);
    }

    /**
     * Get a specific custom bundle by ID.
     * GET /api/custom-bundles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomBundleDto> getBundleById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        try {
            CustomBundleDto bundle = customBundleService.getBundleById(id, userId);
            return ResponseEntity.ok(bundle);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all approved bundles (visible to public).
     * GET /api/custom-bundles/public
     */
    @GetMapping("/public")
    public ResponseEntity<List<CustomBundleDto>> getPublicBundles() {
        List<CustomBundleDto> bundles = customBundleService.getApprovedBundles();
        return ResponseEntity.ok(bundles);
    }

    /**
     * Create a new custom bundle.
     * POST /api/custom-bundles
     */
    @PostMapping
    public ResponseEntity<CustomBundleDto> createBundle(
            @RequestBody CreateBundleRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        CustomBundleDto bundle = customBundleService.createBundle(userId, request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(bundle);
    }

    /**
     * Add a paper to the bundle.
     * POST /api/custom-bundles/{id}/papers/{paperId}
     */
    @PostMapping("/{id}/papers/{paperId}")
    public ResponseEntity<CustomBundleDto> addPaper(
            @PathVariable Long id,
            @PathVariable Long paperId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        CustomBundleDto updated = customBundleService.addPaper(id, paperId, userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Remove a paper from the bundle.
     * DELETE /api/custom-bundles/{id}/papers/{paperId}
     */
    @DeleteMapping("/{id}/papers/{paperId}")
    public ResponseEntity<CustomBundleDto> removePaper(
            @PathVariable Long id,
            @PathVariable Long paperId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        CustomBundleDto updated = customBundleService.removePaper(id, paperId, userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete the bundle.
     * DELETE /api/custom-bundles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBundle(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        customBundleService.deleteBundle(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Purchase the bundle.
     * POST /api/custom-bundles/{id}/purchase
     */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<CustomBundleDto> purchaseBundle(
            @PathVariable Long id,
            @RequestBody com.eduapp.backend.dto.PaymentRequest paymentRequest,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        CustomBundleDto purchased = customBundleService.purchaseBundle(id, userId,
                paymentRequest.getPaymentReference());
        return ResponseEntity.ok(purchased);
    }

    /**
     * Get current price per paper.
     * GET /api/custom-bundles/price-per-paper
     */
    @GetMapping("/price-per-paper")
    public ResponseEntity<PriceResponse> getPricePerPaper() {
        BigDecimal price = customBundleService.getPricePerPaper();
        return ResponseEntity.ok(new PriceResponse(price));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    // DTOs
    public static class CreateBundleRequest {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class PriceResponse {
        private BigDecimal pricePerPaper;

        public PriceResponse(BigDecimal pricePerPaper) {
            this.pricePerPaper = pricePerPaper;
        }

        public BigDecimal getPricePerPaper() {
            return pricePerPaper;
        }

        public void setPricePerPaper(BigDecimal pricePerPaper) {
            this.pricePerPaper = pricePerPaper;
        }
    }
}
