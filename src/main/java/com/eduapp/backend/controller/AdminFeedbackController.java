package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.dto.ImprovementDto;
import com.eduapp.backend.dto.ReviewDto;
import com.eduapp.backend.model.ImprovementStatus;
import com.eduapp.backend.model.ReviewStatus;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.ImprovementService;
import com.eduapp.backend.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * SCALE-4: paginated reviews with optional status filter. ?status=&page=&size= (size cap 100).
     */
    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
            return ResponseEntity.ok(reviewService.getReviewsForAdmin(status, pageable));
        } catch (Exception e) {
            logger.error("Error fetching reviews for admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cheap COUNT-based summary so the admin page can show totals without loading every row.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getFeedbackStats() {
        long pendingReviews = reviewService.countReviewsByStatus(ReviewStatus.PENDING);
        long approvedReviews = reviewService.countReviewsByStatus(ReviewStatus.APPROVED);
        long rejectedReviews = reviewService.countReviewsByStatus(ReviewStatus.REJECTED);
        long pendingImprovements = improvementService.countImprovementsByStatus(ImprovementStatus.PENDING);
        return ResponseEntity.ok(Map.of(
                "pendingReviews", pendingReviews,
                "approvedReviews", approvedReviews,
                "rejectedReviews", rejectedReviews,
                "totalReviews", pendingReviews + approvedReviews + rejectedReviews,
                "pendingImprovements", pendingImprovements,
                "totalImprovements", improvementService.countAllImprovements()));
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
     * SCALE-4: paginated improvements with optional status filter. ?status=&page=&size= (size cap 100).
     */
    @GetMapping("/improvements")
    public ResponseEntity<Page<ImprovementDto>> getAllImprovements(
            @RequestParam(required = false) ImprovementStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
            return ResponseEntity.ok(improvementService.getImprovementsForAdmin(status, pageable));
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
            @Valid @RequestBody Map<String, String> request,
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
