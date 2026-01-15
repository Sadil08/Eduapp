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

    /**
     * Get all paper bundles with pagination, search, and sorting
     * @param page Page number (0-indexed)
     * @param size Page size (max 100)
     * @param search Optional search term for name/description
     * @param sortBy Field to sort by (default: createdAt)
     * @param sortDir Sort direction ASC/DESC (default: DESC)
     */
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<com.eduapp.backend.dto.PaperBundleSummaryDto>> getAllPaperBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        logger.info("Getting all bundles - page: {}, size: {}, search: '{}', sort: {} {}", 
                    page, size, search, sortBy, sortDir);
        
        // Limit max page size
        size = Math.min(size, 100);
        
        // Create sort direction
        org.springframework.data.domain.Sort.Direction direction = 
            sortDir.equalsIgnoreCase("ASC") ? 
                org.springframework.data.domain.Sort.Direction.ASC : 
                org.springframework.data.domain.Sort.Direction.DESC;
        
        // Create pageable
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by(direction, sortBy));
        
        org.springframework.data.domain.Page<com.eduapp.backend.dto.PaperBundleSummaryDto> result;
        
        if (search != null && !search.isBlank()) {
            result = paperBundleService.searchBundles(search, pageable);
        } else {
            result = paperBundleService.getAllSummariesPaginated(pageable);
        }
        
        return ResponseEntity.ok(result);
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

    // Handles GET request to filter bundles by multiple criteria
    @GetMapping("/filter")
    public ResponseEntity<List<com.eduapp.backend.dto.PaperBundleSummaryDto>> filterBundles(
            @RequestParam(required = false) com.eduapp.backend.model.PaperType type,
            @RequestParam(required = false) Long examTypeId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(required = false) Boolean isPastPaper,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String name) {
        logger.info(
                "Filtering bundles - type: {}, examTypeId: {}, subjectId: {}, lessonId: {}, isPastPaper: {}, minPrice: {}, maxPrice: {}, name: {}",
                type, examTypeId, subjectId, lessonId, isPastPaper, minPrice, maxPrice, name);

        List<com.eduapp.backend.dto.PaperBundleSummaryDto> bundles = paperBundleService.filterBundles(
                type, examTypeId, subjectId, lessonId, isPastPaper, minPrice, maxPrice, name);

        return ResponseEntity.ok(bundles);
    }

    // Handles GET request to search bundles by name
    @GetMapping("/search")
    public ResponseEntity<List<com.eduapp.backend.dto.PaperBundleSummaryDto>> searchByName(
            @RequestParam String name) {
        logger.info("Searching bundles by name: {}", name);

        List<com.eduapp.backend.dto.PaperBundleSummaryDto> bundles = paperBundleService.searchByName(name);

        return ResponseEntity.ok(bundles);
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