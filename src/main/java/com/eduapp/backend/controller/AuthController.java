package com.eduapp.backend.controller;

import com.eduapp.backend.dto.JwtResponse;
import com.eduapp.backend.dto.LoginRequest;
import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.dto.UserResponse;
import com.eduapp.backend.dto.ForgotPasswordRequest;
import com.eduapp.backend.dto.ResetPasswordRequest;
import com.eduapp.backend.model.User;
import com.eduapp.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        String ip = getClientIp(request);
        User saved = userService.register(req, ip);
        return ResponseEntity
                .ok(new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        String ip = getClientIp(request);
        String token = userService.login(req.getEmail(), req.getPassword(), ip);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        userService.forgotPassword(req.getEmail());
        return ResponseEntity.ok().body("{\"message\": \"OTP sent to email\"}");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
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