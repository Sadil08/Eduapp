package com.eduapp.backend.controller;

import com.eduapp.backend.model.User;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.PurchaseService;
import com.eduapp.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public PurchaseController(PurchaseService purchaseService, UserService userService, JwtUtil jwtUtil) {
        this.purchaseService = purchaseService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Void> checkout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        purchaseService.checkout(user);
        return ResponseEntity.ok().build();
    }
}
