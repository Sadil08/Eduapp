package com.eduapp.backend.controller;

import com.eduapp.backend.dto.PaperDto;
import com.eduapp.backend.dto.PaperAttemptDto;
import com.eduapp.backend.dto.PaperSubmissionDto;
import com.eduapp.backend.dto.StudentPaperAttemptDto;
import com.eduapp.backend.mapper.PaperMapper;
import com.eduapp.backend.mapper.StudentPaperAttemptMapper;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperType;
import com.eduapp.backend.service.PaperService;
import com.eduapp.backend.service.AIAnalysisService;
import com.eduapp.backend.model.StudentPaperAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.eduapp.backend.security.JwtUtil;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Paper entities.
 * Provides RESTful endpoints for CRUD operations on papers.
 */
@RestController
@RequestMapping("/api/papers")
public class PaperController {

    private static final Logger logger = LoggerFactory.getLogger(PaperController.class);

    private final PaperService paperService;
    private final PaperMapper paperMapper;
    private final AIAnalysisService aiAnalysisService;
    private final JwtUtil jwtUtil;
    private final StudentPaperAttemptMapper studentPaperAttemptMapper;

    public PaperController(PaperService paperService, PaperMapper paperMapper, AIAnalysisService aiAnalysisService,
            JwtUtil jwtUtil, StudentPaperAttemptMapper studentPaperAttemptMapper) {
        this.paperService = paperService;
        this.paperMapper = paperMapper;
        this.aiAnalysisService = aiAnalysisService;
        this.jwtUtil = jwtUtil;
        this.studentPaperAttemptMapper = studentPaperAttemptMapper;
    }

    /**
     * Handles GET request to retrieve all papers (Admin only).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PaperDto>> getAllPapers() {
        logger.info("Received request to get all papers");
        List<Paper> papers = paperService.findAll();
        List<PaperDto> dtos = paperMapper.toDtoList(papers);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Handles GET request to retrieve all available papers (for custom bundle creation).
     * Available to all authenticated users.
     */
    @GetMapping("/available")
    public ResponseEntity<List<PaperDto>> getAllAvailablePapers() {
        logger.info("Received request to get all available papers for custom bundle");
        List<Paper> papers = paperService.findAll();
        List<PaperDto> dtos = paperMapper.toDtoList(papers);
        return ResponseEntity.ok(dtos);
    }

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * SCALE-3: paginated, searchable paper listing — the scalable replacement for
     * {@code /available} (which loads every paper). Used by the custom-bundle paper
     * picker. Returns a {@link org.springframework.data.domain.Page} of {@link PaperDto}.
     */
    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<PaperDto>> searchPapers(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), safeSize,
                org.springframework.data.domain.Sort.by("name").ascending());
        org.springframework.data.domain.Page<PaperDto> result =
                paperService.search(query, pageable).map(paperMapper::toDto);
        return ResponseEntity.ok(result);
    }

    /**
     * Handles GET request to retrieve a specific paper by ID (Admin only).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<PaperDto> getPaperById(@PathVariable Long id) {
        logger.info("Received request to get paper with ID: {}", id);
        Optional<Paper> paper = paperService.findById(id);
        if (paper.isPresent()) {
            PaperDto dto = paperMapper.toDto(paper.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles GET request to attempt a paper.
     * Requires purchase of parent bundle.
     * Returns paper with questions (no correct answers exposed).
     * @param forceNew If true, abandons existing IN_PROGRESS attempt and starts a new one
     * @param bundleId The bundle context for this attempt (optional if customBundleId provided)
     * @param customBundleId The custom bundle context (optional)
     */
    @GetMapping("/{id}/attempt")
    public ResponseEntity<PaperAttemptDto> attemptPaper(@PathVariable Long id,
            @RequestParam(value = "bundleId", required = false) Long bundleId,
            @RequestParam(value = "customBundleId", required = false) Long customBundleId,
            @RequestParam(value = "forceNew", defaultValue = "false") boolean forceNew,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        logger.info("Received request to attempt paper with ID: {} bundleId: {} customBundleId: {} (forceNew={})", 
                id, bundleId, customBundleId, forceNew);
        
        if (bundleId == null && customBundleId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            PaperAttemptDto dto = paperService.getPaperAttempt(id, userId, bundleId, customBundleId, forceNew);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles POST request to submit a completed paper.
     * Saves student answers and triggers async AI analysis.
     * Returns the created attempt with basic info (AI analysis pending).
     * @param bundleId The bundle context for this submission (optional if customBundleId provided)
     * @param customBundleId The custom bundle context (optional)
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<StudentPaperAttemptDto> submitPaper(@PathVariable Long id,
            @RequestParam(value = "bundleId", required = false) Long bundleId,
            @RequestParam(value = "customBundleId", required = false) Long customBundleId,
            @RequestBody PaperSubmissionDto submission,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        logger.info("Received request to submit paper with ID: {} bundleId: {} customBundleId: {}", id, bundleId, customBundleId);
        
        if (bundleId == null && customBundleId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            StudentPaperAttempt savedAttempt = paperService.submitPaperAttempt(id, userId, bundleId, customBundleId, submission);
            // Trigger AI Analysis here (async)
            aiAnalysisService.analyzeAttempt(savedAttempt);

            // Map to DTO and return
            StudentPaperAttemptDto dto = studentPaperAttemptMapper.toDto(savedAttempt);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles GET request to retrieve paper attempt results.
     * Returns attempt with answers, marks, and AI feedback.
     * Students can only access their own attempts.
     */
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<StudentPaperAttemptDto> getAttemptResults(@PathVariable Long attemptId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        logger.info("Received request to get attempt results for attempt ID: {}", attemptId);
        try {
            StudentPaperAttemptDto dto = paperService.getAttemptResults(attemptId, userId);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles POST request to create a new paper (Admin only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaperDto> createPaper(@RequestBody PaperDto dto) {
        logger.info("Received request to create paper: {}", dto.getName());
        Paper paper = paperMapper.toEntity(dto);
        Paper savedPaper = paperService.save(paper);
        PaperDto savedDto = paperMapper.toDto(savedPaper);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    /**
     * Handles PUT request to update an existing paper (Admin only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaperDto> updatePaper(@PathVariable Long id, @RequestBody PaperDto dto) {
        logger.info("Received request to update paper with ID: {}", id);
        try {
            Paper updatedPaper = paperService.updatePaper(id, dto);
            PaperDto updatedDto = paperMapper.toDto(updatedPaper);
            return ResponseEntity.ok(updatedDto);
        } catch (IllegalArgumentException e) {
            logger.warn("Paper update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles DELETE request to delete a paper (Admin only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePaper(@PathVariable Long id) {
        logger.info("Received request to delete paper with ID: {}", id);
        if (paperService.existsById(id)) {
            paperService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles GET request to retrieve attempt information for multiple papers.
     * Returns attempt counts and limits for each paper for the current user.
     * GET /api/papers/attempt-info?paperIds=1,2,3&bundleId=5
     * @param bundleId Optional bundle context for bundle-scoped attempt counting
     * @param customBundleId Optional custom bundle context
     */
    @GetMapping("/attempt-info")
    public ResponseEntity<java.util.Map<Long, com.eduapp.backend.dto.PaperAttemptInfoDto>> getAttemptInfo(
            @RequestParam List<Long> paperIds,
            @RequestParam(required = false) Long bundleId,
            @RequestParam(required = false) Long customBundleId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        logger.info("Received request to get attempt info for {} papers for user {} (bundleId={}, customBundleId={})", 
                paperIds.size(), userId, bundleId, customBundleId);
                
        // NOTE: Need to update paperService.getAttemptInfoForPapers to accept customBundleId
        // Currently bypassing passed customBundleId to getAttemptInfoForPapers as it's not yet overloaded there?
        // Wait, I missed updating PaperService.getAttemptInfoForPapers!
        // I will update it in next step. For now leaving it as is but accepting the param to avoid compile error if I call it.
        // Actually, to make it work, I must update the service too.
        // Delegating to existing service method for now (will fix service method next)
        // Oops, cannot modify service method signature via controller edit.
        // I'll leave the call as is for now and assume I'll update service next.
        // Or better, update controller call assuming service IS updated.
        // I'll update logic assuming I WILL update service.
        
        java.util.Map<Long, com.eduapp.backend.dto.PaperAttemptInfoDto> attemptInfo = paperService
                .getAttemptInfoForPapers(paperIds, userId, bundleId, customBundleId);

        return ResponseEntity.ok(attemptInfo);
    }
}