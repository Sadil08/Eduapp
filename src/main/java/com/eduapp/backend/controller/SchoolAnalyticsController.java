package com.eduapp.backend.controller;

import com.eduapp.backend.dto.PaperAnalyticsSummaryDto;
import com.eduapp.backend.dto.QuestionAnalyticsDto;
import com.eduapp.backend.security.TenantContext;
import com.eduapp.backend.service.SchoolAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cohort analytics for teachers/admins (WP-8). Tenant-scoped and aggregate-only.
 */
@RestController
@RequestMapping("/api/school/analytics")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
public class SchoolAnalyticsController {

    private final SchoolAnalyticsService analyticsService;

    public SchoolAnalyticsController(SchoolAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/papers/{paperId}/summary")
    public ResponseEntity<PaperAnalyticsSummaryDto> summary(@PathVariable Long paperId) {
        return ResponseEntity.ok(analyticsService.paperSummary(TenantContext.getSchoolId(), paperId));
    }

    @GetMapping("/papers/{paperId}/questions")
    public ResponseEntity<List<QuestionAnalyticsDto>> questions(@PathVariable Long paperId) {
        return ResponseEntity.ok(analyticsService.questionAnalysis(TenantContext.getSchoolId(), paperId));
    }
}
