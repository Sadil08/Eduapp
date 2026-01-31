package com.eduapp.backend.controller;

import com.eduapp.backend.dto.ImprovementDto;
import com.eduapp.backend.dto.ImprovementSubmissionDto;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.ImprovementService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/improvements")
@PreAuthorize("hasRole('STUDENT')")
public class ImprovementController {

    private static final Logger logger = LoggerFactory.getLogger(ImprovementController.class);

    private final ImprovementService improvementService;
    private final JwtUtil jwtUtil;

    public ImprovementController(ImprovementService improvementService, JwtUtil jwtUtil) {
        this.improvementService = improvementService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Submit an improvement suggestion
     */
    @PostMapping
    public ResponseEntity<ImprovementDto> submitImprovement(
            @Valid @RequestBody ImprovementSubmissionDto dto,
            @RequestHeader("Authorization") String authHeader) {

        logger.info("Received improvement submission request");

        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            ImprovementDto improvement = improvementService.submitImprovement(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(improvement);

        } catch (Exception e) {
            logger.error("Error submitting improvement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user's own improvement suggestions
     */
    @GetMapping("/my")
    public ResponseEntity<List<ImprovementDto>> getMyImprovements(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            List<ImprovementDto> improvements = improvementService.getUserImprovements(userId);
            return ResponseEntity.ok(improvements);

        } catch (Exception e) {
            logger.error("Error fetching user improvements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
