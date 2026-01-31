package com.eduapp.backend.service;

import com.eduapp.backend.dto.analytics.*;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analytics calculations and aggregations
 */
@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final UserRepository userRepository;
    private final StudentBundleAccessRepository bundleAccessRepository;
    private final StudentPaperAttemptRepository attemptRepository;
    private final QuestionExtractionTrackingRepository extractionRepository;
    private final ProgressRepository progressRepository;
    private final LeaderboardEntryRepository leaderboardRepository;
    private final CustomBundleRepository customBundleRepository;

    public AnalyticsService(
            UserRepository userRepository,
            StudentBundleAccessRepository bundleAccessRepository,
            StudentPaperAttemptRepository attemptRepository,
            QuestionExtractionTrackingRepository extractionRepository,
            ProgressRepository progressRepository,
            LeaderboardEntryRepository leaderboardRepository,
            CustomBundleRepository customBundleRepository) {
        this.userRepository = userRepository;
        this.bundleAccessRepository = bundleAccessRepository;
        this.attemptRepository = attemptRepository;
        this.extractionRepository = extractionRepository;
        this.progressRepository = progressRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.customBundleRepository = customBundleRepository;
    }

    /**
     * Get analytics overview statistics (cached for 10 minutes)
     */
    @Cacheable(value = "analytics:overview", unless = "#result == null")
    public AnalyticsOverviewDto getOverview() {
        logger.info("Fetching analytics overview");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();

        // User metrics
        Long totalUsers = userRepository.count();
        Long activeUsersToday = (long) userRepository.findActiveUsersSince(startOfToday).size();
        Long activeUsersThisMonth = (long) userRepository.findActiveUsersSince(startOfMonth).size();

        // Revenue metrics - Standard bundles
        BigDecimal standardTotalRevenue = bundleAccessRepository.sumPricePaid();
        if (standardTotalRevenue == null) standardTotalRevenue = BigDecimal.ZERO;

        BigDecimal standardRevenueToday = bundleAccessRepository.sumRevenueonDate(today);
        if (standardRevenueToday == null) standardRevenueToday = BigDecimal.ZERO;

        BigDecimal standardRevenueThisMonth = bundleAccessRepository.sumRevenueThisMonth(startOfMonth);
        if (standardRevenueThisMonth == null) standardRevenueThisMonth = BigDecimal.ZERO;

        // Revenue metrics - Custom bundles
        BigDecimal customTotalRevenue = customBundleRepository.sumTotalRevenue();
        if (customTotalRevenue == null) customTotalRevenue = BigDecimal.ZERO;

        BigDecimal customRevenueToday = customBundleRepository.sumRevenueOnDate(today);
        if (customRevenueToday == null) customRevenueToday = BigDecimal.ZERO;

        BigDecimal customRevenueThisMonth = customBundleRepository.sumRevenueThisMonth(startOfMonth);
        if (customRevenueThisMonth == null) customRevenueThisMonth = BigDecimal.ZERO;

        // Combined revenue
        BigDecimal totalRevenue = standardTotalRevenue.add(customTotalRevenue);
        BigDecimal revenueToday = standardRevenueToday.add(customRevenueToday);
        BigDecimal revenueThisMonth = standardRevenueThisMonth.add(customRevenueThisMonth);

        // Extraction metrics
        Long totalExtractions = extractionRepository.sumAllExtractionCounts();
        if (totalExtractions == null) totalExtractions = 0L;

        Long extractionsToday = extractionRepository.sumTodayExtractionCounts(today.atStartOfDay());
        if (extractionsToday == null) extractionsToday = 0L;

        // Bundle sales - Standard + Custom
        Long standardBundlesSold = bundleAccessRepository.countTotalBundlesSold();
        if (standardBundlesSold == null) standardBundlesSold = 0L;

        Long customBundlesSold = customBundleRepository.countPurchasedBundles();
        if (customBundlesSold == null) customBundlesSold = 0L;

        Long totalBundlesSold = standardBundlesSold + customBundlesSold;

        // Country diversity
        Integer totalCountries = userRepository.countDistinctCountries();
        if (totalCountries == null) totalCountries = 0;

        logger.info("Analytics overview: standardRevenue={}, customRevenue={}, totalRevenue={}, standardBundles={}, customBundles={}", 
            standardTotalRevenue, customTotalRevenue, totalRevenue, standardBundlesSold, customBundlesSold);

        return new AnalyticsOverviewDto(
                totalUsers,
                activeUsersToday,
                activeUsersThisMonth,
                totalRevenue,
                revenueToday,
                revenueThisMonth,
                totalExtractions,
                extractionsToday,
                totalBundlesSold,
                totalCountries
        );
    }

    /**
     * Get detailed user analytics with filters
     */
    public List<UserAnalyticsDto> getUserAnalytics() {
        logger.info("Fetching user analytics");

        List<User> users = userRepository.findAll();
        Map<Long, BigDecimal> revenuePerUser = getRevenuePerUserMap();
        Map<Long, Long> extractionsPerUser = getExtractionsPerUserMap();
        Map<Long, Long> attemptsPerUser = getAttemptsPerUserMap();
        Map<Long, Double> avgScorePerUser = getAvgScorePerUserMap();

        return users.stream().map(user -> {
            Long userId = user.getId();
            Long bundlesPurchased = (long) bundleAccessRepository.findByStudentId(userId).size();
            BigDecimal totalSpent = revenuePerUser.getOrDefault(userId, BigDecimal.ZERO);
            Long totalExtractions = extractionsPerUser.getOrDefault(userId, 0L);
            Long totalAttempts = attemptsPerUser.getOrDefault(userId, 0L);
            Double avgScore = avgScorePerUser.getOrDefault(userId, 0.0);

            return new UserAnalyticsDto(
                    userId,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getCountry(),
                    user.getCreatedAt(),
                    user.getLastLoginTime(),
                    bundlesPurchased,
                    totalSpent,
                    totalExtractions,
                    totalAttempts,
                    avgScore
            );
        }).collect(Collectors.toList());
    }

    /**
     * Get paginated user analytics with filters (optimized version with direct query)
     */
    public org.springframework.data.domain.Page<UserAnalyticsDto> getUserAnalyticsPaginated(
            org.springframework.data.domain.Pageable pageable,
            String country,
            String searchTerm) {
        logger.info("Fetching paginated user analytics with optimized query - country: {}, search: '{}'", 
                    country, searchTerm);
        
        // Use optimized repository query (single query instead of N+1)
        // Lowercase and add wildcards here to avoid "lower(bytea)" inference issues in JPQL
        String searchPattern = (searchTerm != null && !searchTerm.isEmpty()) ? "%" + searchTerm.toLowerCase() + "%" : null;
        return userRepository.findUserAnalyticsOptimized(country, searchPattern, pageable);
    }

    /**
     * Get geographical statistics grouped by country (cached for 10 minutes)
     */
    @Cacheable(value = "analytics:geographical", unless = "#result == null")
    public List<GeographicalStatsDto> getGeographicalStats() {
        logger.info("Fetching geographical analytics");

        List<Object[]> usersByCountry = userRepository.countUsersByCountry();
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Object[]> activeByCountry = userRepository.countActiveUsersByCountrySince(oneMonthAgo);
        
        // Standard bundle revenue by country
        List<Object[]> standardRevenueByCountry = bundleAccessRepository.getRevenueByCountry();
        // Custom bundle revenue by country
        List<Object[]> customRevenueByCountry = customBundleRepository.getRevenueByCountry();
        
        Map<String, Long> extractionsByCountry = getExtractionsByCountryMap();

        // Create maps for quick lookup
        Map<String, Long> activeCountMap = activeByCountry.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        // Combine standard and custom bundle revenue by country
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        standardRevenueByCountry.forEach(row -> {
            String country = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.merge(country, revenue, BigDecimal::add);
        });
        customRevenueByCountry.forEach(row -> {
            String country = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.merge(country, revenue, BigDecimal::add);
        });

        return usersByCountry.stream().map(row -> {
            String country = (String) row[0];
            Long userCount = ((Number) row[1]).longValue();
            Long activeUsers = activeCountMap.getOrDefault(country, 0L);
            BigDecimal revenue = revenueMap.getOrDefault(country, BigDecimal.ZERO);
            Long extractions = extractionsByCountry.getOrDefault(country, 0L);

            return new GeographicalStatsDto(country, userCount, activeUsers, revenue, extractions);
        }).collect(Collectors.toList());
    }

    /**
     * Get daily revenue data for charts (cached for 10 minutes)
     */
    @Cacheable(value = "analytics:revenue", key = "#startDate + '-' + #endDate", unless = "#result == null")
    public List<DailyRevenueDto> getDailyRevenue(String startDate, String endDate) {
        logger.info("Fetching daily revenue from {} to {}", startDate, endDate);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        // Standard bundle daily revenue
        List<Object[]> standardDailyData = bundleAccessRepository.getDailyRevenue(start, end);
        // Custom bundle daily revenue
        List<Object[]> customDailyData = customBundleRepository.getDailyRevenue(start, end);

        // Combine by date
        Map<LocalDate, DailyRevenueDto> revenueByDate = new TreeMap<>();
        
        standardDailyData.forEach(row -> {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            BigDecimal revenue = (BigDecimal) row[1];
            Long transactions = ((Number) row[2]).longValue();
            Long uniqueUsers = ((Number) row[3]).longValue();
            revenueByDate.put(date, new DailyRevenueDto(date, revenue, transactions, uniqueUsers));
        });
        
        customDailyData.forEach(row -> {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            BigDecimal revenue = (BigDecimal) row[1];
            Long transactions = ((Number) row[2]).longValue();
            Long uniqueUsers = ((Number) row[3]).longValue();
            
            if (revenueByDate.containsKey(date)) {
                DailyRevenueDto existing = revenueByDate.get(date);
                revenueByDate.put(date, new DailyRevenueDto(
                    date,
                    existing.getRevenue().add(revenue),
                    existing.getTransactions() + transactions,
                    existing.getUniqueUsers() + uniqueUsers // Note: This might overcount if same user bought both
                ));
            } else {
                revenueByDate.put(date, new DailyRevenueDto(date, revenue, transactions, uniqueUsers));
            }
        });

        return new ArrayList<>(revenueByDate.values());
    }

    /**
     * Get extraction usage statistics
     */
    public ExtractionStatsDto getExtractionStats() {
        logger.info("Fetching extraction analytics");

        Long totalExtractions = extractionRepository.sumAllExtractionCounts();
        if (totalExtractions == null) totalExtractions = 0L;

        Long extractionsToday = extractionRepository.sumTodayExtractionCounts(LocalDate.now().atStartOfDay());
        if (extractionsToday == null) extractionsToday = 0L;

        Map<Long, Long> extractionsByUser = getExtractionsPerUserMap();
        Long uniqueUsers = (long) extractionsByUser.size();

        Long usersHitLimit = extractionRepository.countUsersHitLimit();
        if (usersHitLimit == null) usersHitLimit = 0L;

        return new ExtractionStatsDto(
                totalExtractions,
                uniqueUsers,
                extractionsToday,
                extractionsByUser,
                usersHitLimit
        );
    }

    /**
     * Get bundle performance statistics
     */
    public List<BundlePerformanceDto> getBundlePerformance() {
        logger.info("Fetching bundle performance analytics");

        List<Object[]> bundleStats = bundleAccessRepository.getBundlePurchaseStats();

        return bundleStats.stream().map(row -> {
            Long bundleId = ((Number) row[0]).longValue();
            String bundleName = (String) row[1];
            Long purchases = ((Number) row[2]).longValue();
            BigDecimal revenue = (BigDecimal) row[3];

            // Calculate completion rates and avg scores (simplified - can be enhanced)
            Double completionRate = 0.0; // TODO: Calculate from progress data
            Double avgScore = 0.0; // TODO: Calculate from leaderboard data

            return new BundlePerformanceDto(bundleId, bundleName, purchases, revenue, completionRate, avgScore);
        }).collect(Collectors.toList());
    }

    /**
     * Get daily user activity
     */
    public List<DailyUserActivityDto> getDailyUserActivity(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching daily user activity from {} to {}", startDate, endDate);

        List<DailyUserActivityDto> activities = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Long registrations = userRepository.countUsersRegisteredOnDate(date);
            if (registrations == null) registrations = 0L;

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            Long activeUsers = (long) userRepository.findActiveUsersSince(dayStart).stream()
                    .filter(u -> u.getLastLoginTime() != null && u.getLastLoginTime().isBefore(dayEnd))
                    .count();

            // Total logins (simplified - could use login history table)
            Long totalLogins = activeUsers; // Approximation

            activities.add(new DailyUserActivityDto(date, registrations, activeUsers, totalLogins));
        }

        return activities;
    }

    // Helper methods
    private Map<Long, BigDecimal> getRevenuePerUserMap() {
        // Standard bundle revenue per user
        List<Object[]> standardResults = bundleAccessRepository.getRevenuePerUser();
        Map<Long, BigDecimal> revenueMap = new HashMap<>();
        standardResults.forEach(row -> {
            Long userId = ((Number) row[0]).longValue();
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.merge(userId, revenue, BigDecimal::add);
        });
        
        // Custom bundle revenue per user
        List<Object[]> customResults = customBundleRepository.getRevenuePerUser();
        customResults.forEach(row -> {
            Long userId = ((Number) row[0]).longValue();
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.merge(userId, revenue, BigDecimal::add);
        });
        
        return revenueMap;
    }

    private Map<Long, Long> getExtractionsPerUserMap() {
        List<Object[]> results = extractionRepository.sumExtractionCountsGroupedByUser();
        return results.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).longValue()
        ));
    }

    private Map<Long, Long> getAttemptsPerUserMap() {
        List<Object[]> results = attemptRepository.countAttemptsByUser();
        return results.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).longValue()
        ));
    }

    private Map<Long, Double> getAvgScorePerUserMap() {
        // Using leaderboard entries as proxy for scores
        List<Object[]> results = leaderboardRepository.avgScoreByUser();
        return results.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).doubleValue()
        ));
    }

    private Map<String, Long> getExtractionsByCountryMap() {
        List<Object[]> results = extractionRepository.sumExtractionCountsByCountry();
        return results.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
        ));
    }
}
