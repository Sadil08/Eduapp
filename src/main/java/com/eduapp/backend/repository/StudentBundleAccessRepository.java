package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eduapp.backend.model.StudentBundleAccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StudentBundleAccessRepository extends JpaRepository<StudentBundleAccess, Long> {
    /**
     * Find all bundle accesses for a specific student
     */
    List<StudentBundleAccess> findByStudentId(Long studentId);

    /**
     * Check if student has access to a bundle
     */
    boolean existsByStudentIdAndBundleId(Long studentId, Long bundleId);

    /**
     * Count number of students with access to a specific bundle
     */
    int countByBundleId(Long bundleId);

    /**
     * Find access record by student and bundle for admin grant/revoke
     */
    StudentBundleAccess findByStudentIdAndBundleId(Long studentId, Long bundleId);

    @Query("SELECT SUM(s.pricePaid) FROM StudentBundleAccess s")
    BigDecimal sumPricePaid();

    // Analytics queries
    @Query("SELECT DATE(sba.purchasedAt), SUM(sba.pricePaid), COUNT(sba), COUNT(DISTINCT sba.student.id) " +
           "FROM StudentBundleAccess sba " +
           "WHERE DATE(sba.purchasedAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(sba.purchasedAt) ORDER BY DATE(sba.purchasedAt)")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDate start, @Param("endDate") LocalDate end);

    @Query("SELECT sba.student.id, SUM(sba.pricePaid) FROM StudentBundleAccess sba " +
           "GROUP BY sba.student.id")
    List<Object[]> getRevenuePerUser();

    @Query("SELECT sba.bundle.id, sba.bundle.name, COUNT(sba), SUM(sba.pricePaid) " +
           "FROM StudentBundleAccess sba " +
           "GROUP BY sba.bundle.id, sba.bundle.name " +
           "ORDER BY COUNT(sba) DESC")
    List<Object[]> getBundlePurchaseStats();

    @Query("SELECT SUM(sba.pricePaid) FROM StudentBundleAccess sba WHERE DATE(sba.purchasedAt) = :date")
    BigDecimal sumRevenueonDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(sba.pricePaid) FROM StudentBundleAccess sba WHERE sba.purchasedAt >= :startOfMonth")
    BigDecimal sumRevenueThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT u.country, SUM(sba.pricePaid) FROM StudentBundleAccess sba " +
           "JOIN sba.student u WHERE u.country IS NOT NULL " +
           "GROUP BY u.country")
    List<Object[]> getRevenueByCountry();

    @Query("SELECT COUNT(sba) FROM StudentBundleAccess sba")
    Long countTotalBundlesSold();
}