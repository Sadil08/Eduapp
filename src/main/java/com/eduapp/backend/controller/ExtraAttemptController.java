package com.eduapp.backend.controller;

import com.eduapp.backend.dto.PaymentResult;
import com.eduapp.backend.model.ExtraAttemptPurchase;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.ExtraAttemptPurchaseRepository;
import com.eduapp.backend.repository.PaperBundleRepository;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller for managing extra attempt purchases.
 * 
 * WALLET_DISABLED: Previously used WalletService.debit() for payments.
 * Now uses PaymentService (PayHere mock) for card-based payments.
 */
@RestController
@RequestMapping("/api/papers/{paperId}/extra-attempts")
public class ExtraAttemptController {

    private final ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository;
    private final PaperRepository paperRepository;
    private final PaperBundleRepository paperBundleRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PaymentService paymentService;

    // WALLET_DISABLED: WalletService replaced with PaymentService
    // private final WalletService walletService;

    public ExtraAttemptController(ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository,
            PaperRepository paperRepository,
            PaperBundleRepository paperBundleRepository,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            PaymentService paymentService) {
        this.extraAttemptPurchaseRepository = extraAttemptPurchaseRepository;
        this.paperRepository = paperRepository;
        this.paperBundleRepository = paperBundleRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.paymentService = paymentService;
    }

    /**
     * Purchase extra attempts for a paper within a specific bundle context
     * POST /api/papers/{paperId}/extra-attempts/purchase?bundleId=X
     */
    @PostMapping("/purchase")
    public ResponseEntity<ExtraAttemptPurchaseDto> purchaseExtraAttempts(
            @PathVariable Long paperId,
            @RequestParam Long bundleId,
            @RequestBody PurchaseExtraAttemptsRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        PaperBundle bundle = paperBundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));

        BigDecimal pricePerAttempt = new BigDecimal("5.00");
        BigDecimal totalPrice = pricePerAttempt.multiply(new BigDecimal(request.getAttemptsCount()));

        // WALLET_DISABLED: Previously used walletService.debit()
        // walletService.debit(user, totalPrice, "Purchase of " +
        // request.getAttemptsCount() + " extra attempts for paper: " + paper.getName()
        // + " in bundle: " + bundle.getName());

        // Verify payment via PayHere gateway
        PaymentResult paymentResult = paymentService.verifyPayment(
                request.getPaymentReference(), totalPrice);
        if (paymentResult.getStatus() != PaymentResult.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment verification failed: " + paymentResult.getMessage());
        }

        String paymentId = paymentResult.getPaymentId();

        ExtraAttemptPurchase purchase = new ExtraAttemptPurchase(
                user,
                paper,
                bundle,
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

    // DTOs
    public static class PurchaseExtraAttemptsRequest {
        private Integer attemptsCount;
        private String paymentReference;

        public Integer getAttemptsCount() {
            return attemptsCount;
        }

        public void setAttemptsCount(Integer attemptsCount) {
            this.attemptsCount = attemptsCount;
        }

        public String getPaymentReference() {
            return paymentReference;
        }

        public void setPaymentReference(String paymentReference) {
            this.paymentReference = paymentReference;
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
