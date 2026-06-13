package com.eduapp.backend.controller;

import com.eduapp.backend.dto.OverrideMarkRequest;
import com.eduapp.backend.dto.SchoolAttemptDto;
import com.eduapp.backend.service.SchoolAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Teacher review of school attempts (WP-7): list submissions for a paper and override the
 * AI mark with a mandatory justification note. All tenant-scoped.
 */
@RestController
@RequestMapping("/api/school/attempts")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
public class SchoolAttemptReviewController {

    private final SchoolAttemptService attemptService;

    public SchoolAttemptReviewController(SchoolAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @GetMapping
    public ResponseEntity<List<SchoolAttemptDto>> forPaper(@RequestParam Long paperId) {
        return ResponseEntity.ok(attemptService.listForPaper(paperId).stream().map(SchoolAttemptDto::from).toList());
    }

    @PostMapping("/{id}/override")
    public ResponseEntity<SchoolAttemptDto> override(@PathVariable Long id, @RequestBody OverrideMarkRequest req) {
        return ResponseEntity.ok(SchoolAttemptDto.from(
                attemptService.teacherOverride(id, req.getMark(), req.getNote())));
    }
}
