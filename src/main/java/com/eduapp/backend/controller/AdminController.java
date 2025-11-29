package com.eduapp.backend.controller;

import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.model.User;
import com.eduapp.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')") // 👈 restricts to admin users
    public ResponseEntity<User> createAdmin(@RequestBody RegisterRequest req) {
        User created = userService.createAdmin(req);
        return ResponseEntity.ok(created);
    }
}
