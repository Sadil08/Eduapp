package com.eduapp.backend.controller;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.AdminPaperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for admin paper and question management.
 * All endpoints require ADMIN role.
 * Provides CRUD operations for papers and questions.
 */
@RestController
@RequestMapping("/api/admin/papers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaperController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPaperController.class);

    private final AdminPaperService adminPaperService;
    private final JwtUtil jwtUtil;

    public AdminPaperController(AdminPaperService adminPaperService, JwtUtil jwtUtil) {
        this.adminPaperService = adminPaperService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get all papers with statistics
     * GET /api/admin/papers
     */
    @GetMapping
    public ResponseEntity<List<AdminPaperDto>> getAllPapers() {
        logger.info("Admin requested all papers");
        List<AdminPaperDto> papers = adminPaperService.getAllPapers();
        return ResponseEntity.ok(papers);
    }

    /**
     * Get paper details with questions
     * GET /api/admin/papers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminPaperDto> getPaperDetails(@PathVariable Long id) {
        logger.info("Admin requested paper details for ID: {}", id);
        try {
            AdminPaperDto paper = adminPaperService.getPaperDetails(id);
            return ResponseEntity.ok(paper);
        } catch (IllegalArgumentException e) {
            logger.warn("Paper not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new paper
     * POST /api/admin/papers
     */
    @PostMapping
    public ResponseEntity<Paper> createPaper(
            @RequestBody PaperDto dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        Long adminId = jwtUtil.extractUserId(token);

        logger.info("Admin {} creating paper: {}", adminId, dto.getName());

        try {
            Paper created = adminPaperService.createPaper(dto, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating paper", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing paper
     * PUT /api/admin/papers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Paper> updatePaper(
            @PathVariable Long id,
            @RequestBody PaperDto dto) {

        logger.info("Admin updating paper ID: {}", id);

        try {
            Paper updated = adminPaperService.updatePaper(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Paper not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating paper", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a paper
     * DELETE /api/admin/papers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaper(@PathVariable Long id) {
        logger.info("Admin deleting paper ID: {}", id);

        try {
            adminPaperService.deletePaper(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Paper not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting paper", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add a question to a paper
     * POST /api/admin/papers/{id}/questions
     */
    @PostMapping("/{id}/questions")
    public ResponseEntity<Question> addQuestion(
            @PathVariable Long id,
            @RequestBody QuestionCreateDto dto) {

        logger.info("Admin adding question to paper ID: {}", id);

        try {
            Question created = adminPaperService.addQuestion(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.warn("Paper not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a question
     * PUT /api/admin/papers/{paperId}/questions/{questionId}
     */
    @PutMapping("/{paperId}/questions/{questionId}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Long paperId,
            @PathVariable Long questionId,
            @RequestBody QuestionCreateDto dto) {

        logger.info("Admin updating question {} in paper {}", questionId, paperId);

        try {
            Question updated = adminPaperService.updateQuestion(paperId, questionId, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Question or paper not found: paper={}, question={}", paperId, questionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a question
     * DELETE /api/admin/papers/{paperId}/questions/{questionId}
     */
    @DeleteMapping("/{paperId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long paperId,
            @PathVariable Long questionId) {

        logger.info("Admin deleting question {} from paper {}", questionId, paperId);

        try {
            adminPaperService.deleteQuestion(paperId, questionId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Question not found or doesn't belong to paper: paper={}, question={}",
                    paperId, questionId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error deleting question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
