package com.eduapp.backend.controller;

import com.eduapp.backend.dto.StudentPaperAttemptDto;
import com.eduapp.backend.mapper.StudentPaperAttemptMapper;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.service.StudentPaperAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing StudentPaperAttempt entities.
 */
@RestController
@RequestMapping("/api/student-paper-attempts")
public class StudentPaperAttemptController {

    private static final Logger logger = LoggerFactory.getLogger(StudentPaperAttemptController.class);

    private final StudentPaperAttemptService attemptService;
    private final StudentPaperAttemptMapper attemptMapper;
    private final com.eduapp.backend.security.JwtUtil jwtUtil;

    public StudentPaperAttemptController(StudentPaperAttemptService attemptService,
            StudentPaperAttemptMapper attemptMapper,
            com.eduapp.backend.security.JwtUtil jwtUtil) {
        this.attemptService = attemptService;
        this.attemptMapper = attemptMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<StudentPaperAttemptDto>> getAllAttempts() {
        logger.info("Received request to get all student paper attempts");
        List<StudentPaperAttempt> attempts = attemptService.findAll();
        List<StudentPaperAttemptDto> dtos = attemptMapper.toDtoList(attempts);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentPaperAttemptDto> getAttemptById(@PathVariable Long id) {
        logger.info("Received request to get student paper attempt with ID: {}", id);
        Optional<StudentPaperAttempt> attempt = attemptService.findById(id);
        if (attempt.isPresent()) {
            StudentPaperAttemptDto dto = attemptMapper.toDto(attempt.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<StudentPaperAttemptDto> createAttempt(@RequestBody StudentPaperAttemptDto dto) {
        logger.info("Received request to create student paper attempt");
        StudentPaperAttempt attempt = attemptMapper.toEntity(dto);
        StudentPaperAttempt savedAttempt = attemptService.save(attempt);
        StudentPaperAttemptDto savedDto = attemptMapper.toDto(savedAttempt);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttempt(@PathVariable Long id) {
        logger.info("Received request to delete student paper attempt with ID: {}", id);
        if (attemptService.existsById(id)) {
            attemptService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves attempt history summaries for a specific paper.
     * Returns lightweight summaries suitable for list views.
     * Security: Only returns attempts belonging to the authenticated user.
     * 
     * @param paperId    the ID of the paper
     * @param authHeader JWT token in Authorization header
     * @return list of attempt summaries ordered by start time (newest first)
     */
    @GetMapping("/paper/{paperId}/history")
    public ResponseEntity<List<com.eduapp.backend.dto.StudentPaperAttemptSummaryDto>> getAttemptHistory(
            @PathVariable Long paperId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header for history request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        if (userId == null) {
            logger.warn("Invalid token for history request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("Fetching attempt history summaries for user ID: {} and paper ID: {}", userId, paperId);
        List<com.eduapp.backend.dto.StudentPaperAttemptSummaryDto> summaries = attemptService
                .getAttemptSummaries(userId, paperId);

        return ResponseEntity.ok(summaries);
    }
}