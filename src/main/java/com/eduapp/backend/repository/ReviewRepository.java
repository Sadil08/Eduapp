package com.eduapp.backend.repository;

import com.eduapp.backend.model.Review;
import com.eduapp.backend.model.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(ReviewStatus status);

    @Query("SELECT r FROM Review r WHERE r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsForPublic(Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.createdAt > :since")
    List<Review> findRecentReviewsByUser(Long userId, LocalDateTime since);

    List<Review> findAllByOrderByCreatedAtDesc();
}
