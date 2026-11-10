package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.model.ExtraAttemptPurchase;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.ExtraAttemptPurchaseRepository;
import com.eduapp.backend.repository.PaperBundleRepository;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller for managing extra attempt purchases.
 * 
 * Purchases go through PaymentController (server-priced PayHere orders); this only reads totals.
 */
@RestController
@RequestMapping("/api/papers/{paperId}/extra-attempts")
public class ExtraAttemptController {

    private final ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository;
    private final PaperRepository paperRepository;
    private final PaperBundleRepository paperBundleRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    public ExtraAttemptController(ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository,
            PaperRepository paperRepository,
            PaperBundleRepository paperBundleRepository,
            UserRepository userRepository,
            JwtUtil jwtUtil) {
        this.extraAttemptPurchaseRepository = extraAttemptPurchaseRepository;
        this.paperRepository = paperRepository;
        this.paperBundleRepository = paperBundleRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get total extra attempts purchased for a paper within a bundle context
     * GET /api/papers/{paperId}/extra-attempts?bundleId=X
     */
    @GetMapping
    public ResponseEntity<ExtraAttemptsInfoDto> getExtraAttempts(
            @PathVariable Long paperId,
            @RequestParam(required = false) Long bundleId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        Integer extraAttempts;
        if (bundleId != null) {
            extraAttempts = extraAttemptPurchaseRepository.sumExtraAttemptsByUserAndPaperAndBundle(userId, paperId,
                    bundleId);
        } else {
            extraAttempts = extraAttemptPurchaseRepository.sumExtraAttemptsByUserAndPaper(userId, paperId);
        }

        ExtraAttemptsInfoDto dto = new ExtraAttemptsInfoDto(extraAttempts != null ? extraAttempts : 0);
        return ResponseEntity.ok(dto);
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
