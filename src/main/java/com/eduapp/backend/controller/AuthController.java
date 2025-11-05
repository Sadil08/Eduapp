package com.eduapp.backend.controller;

import com.eduapp.backend.dto.JwtResponse;
import com.eduapp.backend.dto.LoginRequest;
import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.dto.UserResponse;
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
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req) {
        User saved = userService.register(req);
        return ResponseEntity.ok(new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest req) {
        String token = userService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}