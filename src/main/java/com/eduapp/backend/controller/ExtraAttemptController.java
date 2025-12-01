package com.eduapp.backend.controller;

import com.eduapp.backend.model.ExtraAttemptPurchase;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.ExtraAttemptPurchaseRepository;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Controller for managing extra attempt purchases
 */
@RestController
@RequestMapping("/api/papers/{paperId}/extra-attempts")
public class ExtraAttemptController {

    private final ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository;
    private final PaperRepository paperRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ExtraAttemptController(ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository,
            PaperRepository paperRepository,
            UserRepository userRepository,
            JwtUtil jwtUtil) {
        this.extraAttemptPurchaseRepository = extraAttemptPurchaseRepository;
        this.paperRepository = paperRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Purchase extra attempts for a paper
     * POST /api/papers/{paperId}/extra-attempts/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<ExtraAttemptPurchaseDto> purchaseExtraAttempts(
            @PathVariable Long paperId,
            @RequestBody PurchaseExtraAttemptsRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        // Mock payment - in real implementation, integrate with PayHere
        String paymentId = "MOCK-EXTRA-" + UUID.randomUUID().toString();
        BigDecimal pricePerAttempt = new BigDecimal("5.00"); // Mock price
        BigDecimal totalPrice = pricePerAttempt.multiply(new BigDecimal(request.getAttemptsCount()));

        ExtraAttemptPurchase purchase = new ExtraAttemptPurchase(
                user,
                paper,
                request.getAttemptsCount(),
                totalPrice,
                paymentId);

        ExtraAttemptPurchase saved = extraAttemptPurchaseRepository.save(purchase);

        ExtraAttemptPurchaseDto dto = new ExtraAttemptPurchaseDto(
                saved.getId(),
                saved.getAttemptsGranted(),
                saved.getPricePaid(),
                saved.getPurchasedAt());

        return ResponseEntity.ok(dto);
    }

    /**
     * Get total extra attempts purchased for a paper
     * GET /api/papers/{paperId}/extra-attempts
     */
    @GetMapping
    public ResponseEntity<ExtraAttemptsInfoDto> getExtraAttempts(
            @PathVariable Long paperId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        Integer extraAttempts = extraAttemptPurchaseRepository.sumExtraAttemptsByUserAndPaper(userId, paperId);

        ExtraAttemptsInfoDto dto = new ExtraAttemptsInfoDto(extraAttempts != null ? extraAttempts : 0);
        return ResponseEntity.ok(dto);
    }

    // DTOs
    public static class PurchaseExtraAttemptsRequest {
        private Integer attemptsCount;

        public Integer getAttemptsCount() {
            return attemptsCount;
        }

        public void setAttemptsCount(Integer attemptsCount) {
            this.attemptsCount = attemptsCount;
        }
    }

    public static class ExtraAttemptPurchaseDto {
        private Long id;
        private Integer attemptsGranted;
        private BigDecimal pricePaid;
        private java.time.LocalDateTime purchasedAt;

        public ExtraAttemptPurchaseDto(Long id, Integer attemptsGranted, BigDecimal pricePaid,
                java.time.LocalDateTime purchasedAt) {
            this.id = id;
            this.attemptsGranted = attemptsGranted;
            this.pricePaid = pricePaid;
            this.purchasedAt = purchasedAt;
        }

        public Long getId() {
            return id;
        }

        public Integer getAttemptsGranted() {
            return attemptsGranted;
        }

        public BigDecimal getPricePaid() {
            return pricePaid;
        }

        public java.time.LocalDateTime getPurchasedAt() {
            return purchasedAt;
        }
    }

    public static class ExtraAttemptsInfoDto {
        private Integer totalExtraAttempts;

        public ExtraAttemptsInfoDto(Integer totalExtraAttempts) {
            this.totalExtraAttempts = totalExtraAttempts;
        }

        public Integer getTotalExtraAttempts() {
            return totalExtraAttempts;
        }
    }
}
