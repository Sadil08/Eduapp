package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.dto.StudentPaperAttemptDto;
import com.eduapp.backend.mapper.StudentPaperAttemptMapper;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.security.AttemptAccessGuard;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.service.StudentPaperAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing StudentPaperAttempt entities.
 */
@RestController
@RequestMapping("/api/student-paper-attempts")
// SEC-IDOR (Sprint-0): restrict to consumer/admin roles; per-object ownership is enforced
// per-endpoint via AttemptAccessGuard. Method-level @PreAuthorize on admin endpoints overrides this.
@PreAuthorize("hasAnyRole('STUDENT','SCHOOL_STUDENT','ADMIN')")
public class StudentPaperAttemptController {

    private static final Logger logger = LoggerFactory.getLogger(StudentPaperAttemptController.class);

    private final StudentPaperAttemptService attemptService;
    private final StudentPaperAttemptMapper attemptMapper;
    private final com.eduapp.backend.security.JwtUtil jwtUtil;
    private final AttemptAccessGuard accessGuard;

    public StudentPaperAttemptController(StudentPaperAttemptService attemptService,
            StudentPaperAttemptMapper attemptMapper,
            com.eduapp.backend.security.JwtUtil jwtUtil,
            AttemptAccessGuard accessGuard) {
        this.attemptService = attemptService;
        this.attemptMapper = attemptMapper;
        this.jwtUtil = jwtUtil;
        this.accessGuard = accessGuard;
    }

    // SCALE-4 + data-exposure fix: this dumped EVERY user's attempts to any authenticated
    // caller. Now ADMIN-only and paginated (size cap 100).
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<StudentPaperAttemptDto>> getAllAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        logger.info("Admin requested attempts page {} size {}", page, size);
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(attemptService.findAll(pageable).map(attemptMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentPaperAttemptDto> getAttemptById(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        logger.info("Received request to get student paper attempt with ID: {}", id);
        accessGuard.assertOwnsAttempt(id, principal); // SEC-IDOR
        Optional<StudentPaperAttempt> attempt = attemptService.findById(id);
        if (attempt.isPresent()) {
            StudentPaperAttemptDto dto = attemptMapper.toDto(attempt.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // SEC-ACCESS / SEC-IDOR (Sprint-0): this legacy create path bypasses PaperService — it applies
    // NO purchase/access gate and NO attempt-cap, and the mapper does not bind the student from the
    // token. The real consumer start flow is GET /api/papers/{id}/attempt (PaperService enforces
    // access + cap + ownership). Restricting this endpoint to ADMIN removes the unguarded attack
    // surface without affecting the student flow.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentPaperAttemptDto> createAttempt(@Valid @RequestBody StudentPaperAttemptDto dto) {
        logger.info("Received request to create student paper attempt");
        StudentPaperAttempt attempt = attemptMapper.toEntity(dto);
        StudentPaperAttempt savedAttempt = attemptService.save(attempt);
        StudentPaperAttemptDto savedDto = attemptMapper.toDto(savedAttempt);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    // Admin-only: attempts are counted to enforce the per-paper cap, so a student deleting one
    // would reset their quota. The frontend never calls this.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAttempt(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        logger.info("Received request to delete student paper attempt with ID: {}", id);
        accessGuard.assertOwnsAttempt(id, principal); // SEC-IDOR
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
     * @param bundleId   optional bundle context for filtering bundle-scoped attempts
     * @param authHeader JWT token in Authorization header
     * @return list of attempt summaries ordered by start time (newest first)
     */
    @GetMapping("/paper/{paperId}/history")
    public ResponseEntity<List<com.eduapp.backend.dto.StudentPaperAttemptSummaryDto>> getAttemptHistory(
            @PathVariable Long paperId,
            @RequestParam(required = false) Long bundleId,
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

        logger.info("Fetching attempt history summaries for user ID: {} and paper ID: {} (bundleId={})", 
                userId, paperId, bundleId);
        List<com.eduapp.backend.dto.StudentPaperAttemptSummaryDto> summaries = attemptService
                .getAttemptSummaries(userId, paperId, bundleId);

        return ResponseEntity.ok(summaries);
    }
}