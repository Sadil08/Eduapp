package com.eduapp.backend.repository;

import com.eduapp.backend.model.CustomBundle;
import com.eduapp.backend.model.CustomBundleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomBundleRepository extends JpaRepository<CustomBundle, Long> {

    /**
     * Find the current draft/created bundle for a user.
     * A user can only have one CREATED bundle at a time.
     */
    Optional<CustomBundle> findByCreatorIdAndStatus(Long creatorId, CustomBundleStatus status);

    /**
     * Find all bundles created by a specific user.
     */
    List<CustomBundle> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    /**
     * Find all purchased bundles awaiting approval.
     */
    List<CustomBundle> findByStatusOrderByPurchasedAtAsc(CustomBundleStatus status);

    /**
     * Count how many bundles a user has in CREATED status.
     */
    long countByCreatorIdAndStatus(Long creatorId, CustomBundleStatus status);

    /**
     * Find all approved bundles for public display.
     */
    @Query("SELECT cb FROM CustomBundle cb WHERE cb.status = 'APPROVED' ORDER BY cb.approvedAt DESC")
    List<CustomBundle> findApprovedBundles();

    // ========== Analytics Queries ==========
    // Note: Revenue is tracked when status is PURCHASED or APPROVED (both represent paid bundles)

    /**
     * Sum total revenue from all purchased custom bundles
     */
    @Query("SELECT COALESCE(SUM(cb.totalPrice), 0) FROM CustomBundle cb WHERE cb.status IN ('PURCHASED', 'APPROVED')")
    BigDecimal sumTotalRevenue();

    /**
     * Sum revenue for a specific date
     */
    @Query("SELECT COALESCE(SUM(cb.totalPrice), 0) FROM CustomBundle cb WHERE cb.status IN ('PURCHASED', 'APPROVED') AND CAST(cb.purchasedAt AS LocalDate) = :date")
    BigDecimal sumRevenueOnDate(@Param("date") LocalDate date);

    /**
     * Sum revenue from start of month
     */
    @Query("SELECT COALESCE(SUM(cb.totalPrice), 0) FROM CustomBundle cb WHERE cb.status IN ('PURCHASED', 'APPROVED') AND cb.purchasedAt >= :startOfMonth")
    BigDecimal sumRevenueThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    /**
     * Count total custom bundles sold (purchased or approved)
     */
    @Query("SELECT COUNT(cb) FROM CustomBundle cb WHERE cb.status IN ('PURCHASED', 'APPROVED')")
    Long countPurchasedBundles();

    /**
     * Get daily revenue breakdown for charts
     */
    @Query(value = "SELECT DATE(cb.purchased_at) as date, COALESCE(SUM(cb.total_price), 0) as revenue, COUNT(*) as transactions, COUNT(DISTINCT cb.creator_id) as uniqueUsers " +
           "FROM custom_bundles cb WHERE cb.status IN ('PURCHASED', 'APPROVED') AND DATE(cb.purchased_at) BETWEEN :start AND :end " +
           "GROUP BY DATE(cb.purchased_at) ORDER BY DATE(cb.purchased_at)",
           nativeQuery = true)
    List<Object[]> getDailyRevenue(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Get revenue per user for user analytics
     */
    @Query("SELECT cb.creator.id, SUM(cb.totalPrice) FROM CustomBundle cb WHERE cb.status IN ('PURCHASED', 'APPROVED') GROUP BY cb.creator.id")
    List<Object[]> getRevenuePerUser();

    /**
     * Get revenue grouped by country for geographical analytics
     */
    @Query("SELECT u.country, SUM(cb.totalPrice) FROM CustomBundle cb JOIN cb.creator u WHERE cb.status IN ('PURCHASED', 'APPROVED') AND u.country IS NOT NULL GROUP BY u.country")
    List<Object[]> getRevenueByCountry();
}
