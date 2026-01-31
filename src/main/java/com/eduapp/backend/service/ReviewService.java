package com.eduapp.backend.service;

import com.eduapp.backend.dto.PublicReviewDto;
import com.eduapp.backend.dto.ReviewDto;
import com.eduapp.backend.dto.ReviewSubmissionDto;
import com.eduapp.backend.model.Review;
import com.eduapp.backend.model.ReviewStatus;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.ReviewRepository;
import com.eduapp.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    private static final int REVIEW_LIMIT_DAYS = 30;

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReviewDto submitReview(Long userId, ReviewSubmissionDto dto) {
        logger.info("User {} submitting review", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check rate limiting - 1 review per 30 days
        LocalDateTime since = LocalDateTime.now().minusDays(REVIEW_LIMIT_DAYS);
        List<Review> recentReviews = reviewRepository.findRecentReviewsByUser(userId, since);

        if (!recentReviews.isEmpty()) {
            throw new IllegalStateException(
                    "You can only submit one review every " + REVIEW_LIMIT_DAYS + " days. " +
                            "Please try again later.");
        }

        Review review = new Review(user, dto.getRating(), dto.getReviewText());
        review = reviewRepository.save(review);

        logger.info("Review submitted successfully: ID {}", review.getId());
        return mapToDto(review);
    }

    public List<ReviewDto> getUserReviews(Long userId) {
        logger.info("Fetching reviews for user {}", userId);
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ReviewDto> getAllReviewsForAdmin() {
        logger.info("Fetching all reviews for admin");
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByStatus(ReviewStatus status) {
        logger.info("Fetching reviews with status: {}", status);
        List<Review> reviews = reviewRepository.findByStatusOrderByCreatedAtDesc(status);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto approveReview(Long reviewId, Long adminId) {
        logger.info("Admin {} approving review {}", adminId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setStatus(ReviewStatus.APPROVED);
        review.setAdminReviewedBy(adminId);
        review.setReviewedAt(LocalDateTime.now());

        review = reviewRepository.save(review);
        logger.info("Review {} approved successfully", reviewId);

        return mapToDto(review);
    }

    @Transactional
    public ReviewDto rejectReview(Long reviewId, Long adminId) {
        logger.info("Admin {} rejecting review {}", adminId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setStatus(ReviewStatus.REJECTED);
        review.setAdminReviewedBy(adminId);
        review.setReviewedAt(LocalDateTime.now());

        review = reviewRepository.save(review);
        logger.info("Review {} rejected successfully", reviewId);

        return mapToDto(review);
    }

    public List<PublicReviewDto> getApprovedReviewsForHomepage(int limit) {
        logger.info("Fetching {} approved reviews for homepage", limit);
        List<Review> reviews = reviewRepository.findApprovedReviewsForPublic(
                PageRequest.of(0, limit));
        return reviews.stream().map(this::mapToPublicDto).collect(Collectors.toList());
    }

    private ReviewDto mapToDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getUser().getEmail(),
                review.getRating(),
                review.getReviewText(),
                review.getStatus(),
                review.getCreatedAt(),
                review.getReviewedAt());
    }

    private PublicReviewDto mapToPublicDto(Review review) {
        return new PublicReviewDto(
                review.getUser().getName(),
                review.getRating(),
                review.getReviewText(),
                review.getCreatedAt());
    }
}
