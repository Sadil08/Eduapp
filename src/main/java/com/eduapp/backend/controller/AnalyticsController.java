package com.eduapp.backend.controller;

import com.eduapp.backend.dto.analytics.*;
import com.eduapp.backend.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for admin analytics endpoints
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/admin/analytics/overview
     * Returns dashboard overview with key metrics
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewDto> getOverview() {
        logger.info("Admin requested analytics overview");
        AnalyticsOverviewDto overview = analyticsService.getOverview();
        return ResponseEntity.ok(overview);
    }

    /**
     * GET /api/admin/analytics/users
     * Returns detailed user analytics with pagination
     * 
     * @param page Page number (0-indexed)
     * @param size Page size (max 100)
     * @param country Optional country filter
     * @param searchTerm Optional search term for username/email
     */
    @GetMapping("/users")
    public ResponseEntity<org.springframework.data.domain.Page<UserAnalyticsDto>> getUserAnalytics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String searchTerm) {
        logger.info("Admin requested user analytics (page={}, size={}, country={}, search={})", 
                    page, size, country, searchTerm);
        
        // Limit max page size to prevent memory issues
        size = Math.min(size, 100);
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size);
        
        org.springframework.data.domain.Page<UserAnalyticsDto> userAnalytics = 
            analyticsService.getUserAnalyticsPaginated(pageable, country, searchTerm);
        
        return ResponseEntity.ok(userAnalytics);
    }

    /**
     * GET /api/admin/analytics/geographical
     * Returns geographical statistics by country
     */
    @GetMapping("/geographical")
    public ResponseEntity<List<GeographicalStatsDto>> getGeographicalStats() {
        logger.info("Admin requested geographical analytics");
        List<GeographicalStatsDto> geoStats = analyticsService.getGeographicalStats();
        return ResponseEntity.ok(geoStats);
    }

    /**
     * GET /api/admin/analytics/revenue/daily
     * Returns daily revenue for a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     */
    @GetMapping("/revenue/daily")
    public ResponseEntity<List<DailyRevenueDto>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("Admin requested daily revenue from {} to {}", startDate, endDate);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            logger.warn("Invalid date range: start date {} is after end date {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        List<DailyRevenueDto> dailyRevenue = analyticsService.getDailyRevenue(startDate.toString(), endDate.toString());
        return ResponseEntity.ok(dailyRevenue);
    }

    /**
     * GET /api/admin/analytics/extractions
     * Returns extraction usage statistics
     */
    @GetMapping("/extractions")
    public ResponseEntity<ExtractionStatsDto> getExtractionStats() {
        logger.info("Admin requested extraction analytics");
        ExtractionStatsDto extractionStats = analyticsService.getExtractionStats();
        return ResponseEntity.ok(extractionStats);
    }

    /**
     * GET /api/admin/analytics/bundles
     * Returns bundle performance statistics
     */
    @GetMapping("/bundles")
    public ResponseEntity<List<BundlePerformanceDto>> getBundlePerformance() {
        logger.info("Admin requested bundle performance analytics");
        List<BundlePerformanceDto> bundlePerformance = analyticsService.getBundlePerformance();
        return ResponseEntity.ok(bundlePerformance);
    }

    /**
     * GET /api/admin/analytics/user-activity/daily
     * Returns daily user activity for a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     */
    @GetMapping("/user-activity/daily")
    public ResponseEntity<List<DailyUserActivityDto>> getDailyUserActivity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("Admin requested daily user activity from {} to {}", startDate, endDate);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            logger.warn("Invalid date range: start date {} is after end date {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        // Limit to reasonable date ranges (max 90 days)
        if (startDate.plusDays(90).isBefore(endDate)) {
            logger.warn("Date range too large: {} to {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        List<DailyUserActivityDto> dailyActivity = analyticsService.getDailyUserActivity(startDate, endDate);
        return ResponseEntity.ok(dailyActivity);
    }
}
