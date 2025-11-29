package com.eduapp.backend.controller;

import com.eduapp.backend.dto.PaperBundleDto;
import com.eduapp.backend.service.PaperBundleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.eduapp.backend.security.JwtUtil;
import java.util.List;

/**
 * REST Controller for managing PaperBundle entities.
 * Provides RESTful endpoints for CRUD operations on paper bundles.
 */
@RestController
@RequestMapping("/api/paper-bundles")
public class PaperBundleController {

    private static final Logger logger = LoggerFactory.getLogger(PaperBundleController.class);

    private final PaperBundleService paperBundleService;
    private final JwtUtil jwtUtil;

    public PaperBundleController(PaperBundleService paperBundleService, JwtUtil jwtUtil) {
        this.paperBundleService = paperBundleService;
        this.jwtUtil = jwtUtil;
    }

    // Handles GET request to retrieve all paper bundles, accessible to all users
    // for browsing
    @GetMapping
    public ResponseEntity<List<com.eduapp.backend.dto.PaperBundleSummaryDto>> getAllPaperBundles() {
        logger.info("Received request to get all paper bundles (summary)");
        List<com.eduapp.backend.dto.PaperBundleSummaryDto> dtos = paperBundleService.getAllSummaries();
        return ResponseEntity.ok(dtos);
    }

    // Handles GET request to retrieve bundle details, requires purchase
    @GetMapping("/{id}")
    public ResponseEntity<com.eduapp.backend.dto.PaperBundleDetailDto> getBundleDetails(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        logger.info("Fetching bundle details for ID: {} by user ID: {}", id, userId);

        logger.info("Received request to get bundle details for ID: {}", id);
        try {
            com.eduapp.backend.dto.PaperBundleDetailDto dto = paperBundleService.getBundleDetails(id, userId);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Handles POST request to purchase a bundle
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Void> purchaseBundle(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        logger.info("Purchasing bundle ID: {} by user ID: {}", id, userId);

        logger.info("Received request to purchase bundle with ID: {}", id);
        try {
            paperBundleService.purchaseBundle(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error purchasing bundle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Handles POST request to create a new paper bundle, requires admin
    // authentication

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PaperBundleDto> createPaperBundle(@RequestBody PaperBundleDto dto) {
        logger.info("Received request to create paper bundle: {}", dto.getName());
        PaperBundleDto savedDto = paperBundleService.createBundle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }
}