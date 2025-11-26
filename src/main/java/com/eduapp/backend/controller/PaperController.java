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
     * Handles GET request to retrieve a specific paper by ID (Admin only).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
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
     */
    @GetMapping("/{id}/attempt")
    public ResponseEntity<PaperAttemptDto> attemptPaper(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        logger.info("Received request to attempt paper with ID: {}", id);
        try {
            PaperAttemptDto dto = paperService.getPaperAttempt(id, userId);
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
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<StudentPaperAttemptDto> submitPaper(@PathVariable Long id,
            @RequestBody PaperSubmissionDto submission,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        logger.info("Received request to submit paper with ID: {}", id);
        try {
            StudentPaperAttempt savedAttempt = paperService.submitPaperAttempt(id, userId, submission);
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
        Optional<Paper> existingPaper = paperService.findById(id);
        if (existingPaper.isPresent()) {
            Paper paper = existingPaper.get();
            paper.setName(dto.getName());
            paper.setDescription(dto.getDescription());
            paper.setType(dto.getType());
            paper.setMaxFreeAttempts(dto.getMaxFreeAttempts());
            Paper updatedPaper = paperService.save(paper);
            PaperDto updatedDto = paperMapper.toDto(updatedPaper);
            return ResponseEntity.ok(updatedDto);
        } else {
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
}