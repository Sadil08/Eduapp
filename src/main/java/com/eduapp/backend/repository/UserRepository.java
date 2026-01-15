package com.eduapp.backend.repository;

import com.eduapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByReferralCode(String referralCode);

    /**
     * Optimized query for user analytics with pagination.
     * Single query with JOINs and aggregations instead of N+1 queries.
     * 
     * @param country Optional country filter
     * @param search Optional search term for username/email
     * @param pageable Pagination information
     * @return Page of UserAnalyticsDto
     */
    @Query("SELECT new com.eduapp.backend.dto.analytics.UserAnalyticsDto(" +
           "u.id, u.username, u.email, u.country, u.createdAt, u.lastLoginTime, " +
           "COUNT(DISTINCT sba.id), " +
           "COALESCE(SUM(sba.pricePaid), 0), " +
           "COUNT(DISTINCT qet.id), " +
           "COUNT(DISTINCT spa.id), " +
           "COALESCE(AVG(le.score), 0)) " +
           "FROM User u " +
           "LEFT JOIN StudentBundleAccess sba ON sba.student.id = u.id " +
           "LEFT JOIN StudentPaperAttempt spa ON spa.student.id = u.id " +
           "LEFT JOIN QuestionExtractionTracking qet ON qet.studentPaperAttempt.id = spa.id " +
           "LEFT JOIN LeaderboardEntry le ON le.user.id = u.id " +
           "WHERE (:country IS NULL OR u.country = :country) " +
           "AND (:search IS NULL OR " +
           "     LOWER(u.username) LIKE :search OR " +
           "     LOWER(u.email) LIKE :search) " +
           "GROUP BY u.id, u.username, u.email, u.country, u.createdAt, u.lastLoginTime")
    org.springframework.data.domain.Page<com.eduapp.backend.dto.analytics.UserAnalyticsDto> findUserAnalyticsOptimized(
        @Param("country") String country,
        @Param("search") String search,
        org.springframework.data.domain.Pageable pageable);

    // Analytics queries
    @Query("SELECT u.country, COUNT(u) FROM User u WHERE u.country IS NOT NULL GROUP BY u.country")
    List<Object[]> countUsersByCountry();

    @Query("SELECT u FROM User u WHERE u.lastLoginTime >= :since")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.createdAt) = :date")
    Long countUsersRegisteredOnDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT u.country) FROM User u WHERE u.country IS NOT NULL")
    Integer countDistinctCountries();

    @Query("SELECT u.country, COUNT(u) FROM User u WHERE u.lastLoginTime >= :since AND u.country IS NOT NULL GROUP BY u.country")
    List<Object[]> countActiveUsersByCountrySince(@Param("since") LocalDateTime since);
}