package com.eduapp.backend.controller;

import com.eduapp.backend.dto.PublicReviewDto;
import com.eduapp.backend.dto.ReviewDto;
import com.eduapp.backend.dto.ReviewSubmissionDto;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    public ReviewController(ReviewService reviewService, JwtUtil jwtUtil) {
        this.reviewService = reviewService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Submit a review (STUDENT role required)
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ReviewDto> submitReview(
            @Valid @RequestBody ReviewSubmissionDto dto,
            @RequestHeader("Authorization") String authHeader) {

        logger.info("Received review submission request");

        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            ReviewDto review = reviewService.submitReview(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);

        } catch (IllegalStateException e) {
            logger.warn("Rate limit exceeded: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        } catch (Exception e) {
            logger.error("Error submitting review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user's own reviews
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ReviewDto>> getMyReviews(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            List<ReviewDto> reviews = reviewService.getUserReviews(userId);
            return ResponseEntity.ok(reviews);

        } catch (Exception e) {
            logger.error("Error fetching user reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get approved reviews for homepage (public endpoint)
     */
    @GetMapping("/public")
    public ResponseEntity<List<PublicReviewDto>> getPublicReviews(
            @RequestParam(defaultValue = "15") int limit) {

        try {
            // Cap limit at 50 to prevent abuse
            int safeLimit = Math.min(limit, 50);
            List<PublicReviewDto> reviews = reviewService.getApprovedReviewsForHomepage(safeLimit);
            return ResponseEntity.ok(reviews);

        } catch (Exception e) {
            logger.error("Error fetching public reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
