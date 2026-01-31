package com.eduapp.backend.controller;

import com.eduapp.backend.dto.ImprovementDto;
import com.eduapp.backend.dto.ReviewDto;
import com.eduapp.backend.model.ImprovementStatus;
import com.eduapp.backend.model.ReviewStatus;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.ImprovementService;
import com.eduapp.backend.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/feedback")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(AdminFeedbackController.class);

    private final ReviewService reviewService;
    private final ImprovementService improvementService;
    private final JwtUtil jwtUtil;

    public AdminFeedbackController(ReviewService reviewService, ImprovementService improvementService,
            JwtUtil jwtUtil) {
        this.reviewService = reviewService;
        this.improvementService = improvementService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get all reviews with optional status filter
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDto>> getAllReviews(
            @RequestParam(required = false) ReviewStatus status) {

        try {
            List<ReviewDto> reviews = status != null
                    ? reviewService.getReviewsByStatus(status)
                    : reviewService.getAllReviewsForAdmin();
            return ResponseEntity.ok(reviews);

        } catch (Exception e) {
            logger.error("Error fetching reviews for admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Approve a review
     */
    @PutMapping("/reviews/{id}/approve")
    public ResponseEntity<ReviewDto> approveReview(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            Long adminId = jwtUtil.extractUserId(token);

            ReviewDto review = reviewService.approveReview(id, adminId);
            return ResponseEntity.ok(review);

        } catch (IllegalArgumentException e) {
            logger.warn("Review not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error approving review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reject a review
     */
    @PutMapping("/reviews/{id}/reject")
    public ResponseEntity<ReviewDto> rejectReview(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            Long adminId = jwtUtil.extractUserId(token);

            ReviewDto review = reviewService.rejectReview(id, adminId);
            return ResponseEntity.ok(review);

        } catch (IllegalArgumentException e) {
            logger.warn("Review not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error rejecting review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all improvements with optional status filter
     */
    @GetMapping("/improvements")
    public ResponseEntity<List<ImprovementDto>> getAllImprovements(
            @RequestParam(required = false) ImprovementStatus status) {

        try {
            List<ImprovementDto> improvements = status != null
                    ? improvementService.getImprovementsByStatus(status)
                    : improvementService.getAllImprovementsForAdmin();
            return ResponseEntity.ok(improvements);

        } catch (Exception e) {
            logger.error("Error fetching improvements for admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update improvement status with admin notes
     */
    @PutMapping("/improvements/{id}/status")
    public ResponseEntity<ImprovementDto> updateImprovementStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            Long adminId = jwtUtil.extractUserId(token);

            String statusStr = request.get("status");
            String adminNotes = request.get("adminNotes");

            if (statusStr == null) {
                return ResponseEntity.badRequest().build();
            }

            ImprovementStatus status = ImprovementStatus.valueOf(statusStr);
            ImprovementDto improvement = improvementService.updateImprovementStatus(
                    id, status, adminId, adminNotes);

            return ResponseEntity.ok(improvement);

        } catch (IllegalArgumentException e) {
            logger.warn("Improvement not found or invalid status: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating improvement status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete an improvement (for spam/inappropriate content)
     */
    @DeleteMapping("/improvements/{id}")
    public ResponseEntity<Void> deleteImprovement(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            Long adminId = jwtUtil.extractUserId(token);

            improvementService.deleteImprovement(id, adminId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Improvement not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting improvement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
