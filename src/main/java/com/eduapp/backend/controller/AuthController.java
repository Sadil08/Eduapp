package com.eduapp.backend.controller;

import com.eduapp.backend.dto.JwtResponse;
import com.eduapp.backend.dto.LoginRequest;
import com.eduapp.backend.dto.MeResponse;
import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.dto.UserResponse;
import com.eduapp.backend.dto.ForgotPasswordRequest;
import com.eduapp.backend.dto.ResetPasswordRequest;
import com.eduapp.backend.model.User;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        String ip = getClientIp(request);
        User saved = userService.register(req, ip);
        return ResponseEntity
                .ok(new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        String ip = getClientIp(request);
        String token = userService.login(req.getEmail(), req.getPassword(), ip);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    /**
     * FE-A1.1: server-validated identity. The frontend calls this on load and trusts only its
     * response for role/identity (never a client-decoded JWT). Requires a valid token.
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return userService.findById(principal.getUserId())
                .map(u -> ResponseEntity.ok(new MeResponse(
                        u.getId(), u.getUsername(), u.getEmail(), u.getRole(),
                        u.getSchoolId(), u.getReferralCode())))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        userService.forgotPassword(req.getEmail());
        return ResponseEntity.ok().body("{\"message\": \"OTP sent to email\"}");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        userService.resetPassword(req.getEmail(), req.getOtp(), req.getNewPassword());
        return ResponseEntity.ok().body("{\"message\": \"Password reset successfully\"}");
    }

    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}