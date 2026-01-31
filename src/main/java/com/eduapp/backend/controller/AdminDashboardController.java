package com.eduapp.backend.controller;

import com.eduapp.backend.service.StudentBundleAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final StudentBundleAccessService studentBundleAccessService;

    public AdminDashboardController(StudentBundleAccessService studentBundleAccessService) {
        this.studentBundleAccessService = studentBundleAccessService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, BigDecimal>> getTotalRevenue() {
        BigDecimal totalRevenue = studentBundleAccessService.calculateTotalRevenue();
        return ResponseEntity.ok(Map.of("totalRevenue", totalRevenue));
    }
}
