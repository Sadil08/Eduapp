package com.eduapp.backend.controller;

import com.eduapp.backend.dto.PaperBundleDto;
import com.eduapp.backend.service.PaperBundleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public PaperBundleController(PaperBundleService paperBundleService) {
        this.paperBundleService = paperBundleService;
    }

    // Handles GET request to retrieve all paper bundles, accessible to all users for browsing
    @GetMapping
    public ResponseEntity<List<PaperBundleDto>> getAllPaperBundles() {
        logger.info("Received request to get all paper bundles");
        List<PaperBundleDto> dtos = paperBundleService.getAll();
        return ResponseEntity.ok(dtos);
    }

    // Handles POST request to create a new paper bundle, requires admin authentication
    @PostMapping
    public ResponseEntity<PaperBundleDto> createPaperBundle(@RequestBody PaperBundleDto dto) {
        logger.info("Received request to create paper bundle: {}", dto.getName());
        PaperBundleDto savedDto = paperBundleService.createBundle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }
}