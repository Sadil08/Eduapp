package com.eduapp.backend.controller;

import com.eduapp.backend.model.WalletTransaction;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final JwtUtil jwtUtil;

    public WalletController(WalletService walletService, JwtUtil jwtUtil) {
        this.walletService = walletService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(walletService.getTransactions(userId));
    }

    @PostMapping("/topup")
    public ResponseEntity<Void> topUp(@RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, BigDecimal> request) {
        Long userId = extractUserId(authHeader);
        BigDecimal amount = request.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }
        walletService.topUp(userId, amount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/referral-percentage")
    public ResponseEntity<BigDecimal> getReferralPercentage() {
        return ResponseEntity.ok(walletService.getReferralPercentage());
    }

    @PostMapping("/referral-percentage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setReferralPercentage(@RequestBody Map<String, BigDecimal> request) {
        BigDecimal percentage = request.get("percentage");
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().build();
        }
        walletService.setReferralPercentage(percentage);
        return ResponseEntity.ok().build();
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
