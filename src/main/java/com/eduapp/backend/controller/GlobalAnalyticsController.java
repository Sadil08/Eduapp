package com.eduapp.backend.controller;

import com.eduapp.backend.dto.GlobalScoreAggregateDto;
import com.eduapp.backend.service.GlobalAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Cambridge-shareable aggregate analytics API (WP-9). ADMIN-only (also gated by the
 * {@code /api/analytics/global/**} rule in SecurityConfig). Returns anonymised aggregates
 * only, filtered to consenting global-tier students, with a minimum cohort floor.
 */
@RestController
@RequestMapping("/api/analytics/global")
@PreAuthorize("hasRole('ADMIN')")
public class GlobalAnalyticsController {

    private final GlobalAnalyticsService globalAnalyticsService;

    public GlobalAnalyticsController(GlobalAnalyticsService globalAnalyticsService) {
        this.globalAnalyticsService = globalAnalyticsService;
    }

    @GetMapping("/papers/{paperId}/score-aggregate")
    public ResponseEntity<GlobalScoreAggregateDto> paperScoreAggregate(@PathVariable Long paperId) {
        return ResponseEntity.ok(globalAnalyticsService.paperScoreAggregate(paperId));
    }
}
