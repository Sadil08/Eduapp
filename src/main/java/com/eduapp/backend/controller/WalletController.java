package com.eduapp.backend.controller;

import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * WALLET_DISABLED: All wallet endpoints are disabled.
 * 
 * When re-enabling the wallet feature:
 * 1. Restore the original endpoint implementations (see git history)
 * 2. Remove the 501 responses
 * 3. Re-enable wallet debit in PurchaseService, ExtraAttemptController,
 * CustomBundleService
 * 4. Re-enable wallet UI in frontend (Header, cart page, wallet page, referral
 * section)
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final JwtUtil jwtUtil;

    public WalletController(WalletService walletService, JwtUtil jwtUtil) {
        this.walletService = walletService;
        this.jwtUtil = jwtUtil;
    }

    // WALLET_DISABLED: Returns zero balance for backward compatibility
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(BigDecimal.ZERO);
    }

    // WALLET_DISABLED: Transactions endpoint disabled
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, String>> getTransactions(
            @RequestHeader("Authorization") String authHeader,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message",
                        "Wallet feature is currently unavailable. Payments are processed via PayHere."));
    }

    // WALLET_DISABLED: Top-up endpoint disabled
    @PostMapping("/topup")
    public ResponseEntity<Map<String, String>> topUp(@RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, BigDecimal> request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message",
                        "Wallet top-up is currently unavailable. Payments are processed directly via PayHere."));
    }

    // WALLET_DISABLED: Referral percentage getter disabled
    @GetMapping("/referral-percentage")
    public ResponseEntity<Map<String, String>> getReferralPercentage() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Referral system is currently disabled."));
    }

    // WALLET_DISABLED: Referral percentage setter disabled
    @PostMapping("/referral-percentage")
    public ResponseEntity<Map<String, String>> setReferralPercentage(@RequestBody Map<String, BigDecimal> request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Referral system is currently disabled."));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
