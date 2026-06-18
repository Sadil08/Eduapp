package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.dto.OverrideMarkRequest;
import com.eduapp.backend.dto.SchoolAttemptDto;
import com.eduapp.backend.repository.UserRepository;
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
    private final UserRepository userRepository;

    public SchoolAttemptReviewController(SchoolAttemptService attemptService, UserRepository userRepository) {
        this.attemptService = attemptService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<SchoolAttemptDto>> forPaper(@RequestParam Long paperId) {
        return ResponseEntity.ok(attemptService.listForPaper(paperId).stream()
                .map(a -> SchoolAttemptDto.from(a, userRepository.findById(a.getStudentId()).orElse(null)))
                .toList());
    }

    @PostMapping("/{id}/override")
    public ResponseEntity<SchoolAttemptDto> override(@PathVariable Long id, @Valid @RequestBody OverrideMarkRequest req) {
        return ResponseEntity.ok(SchoolAttemptDto.from(
                attemptService.teacherOverride(id, req.getMark(), req.getNote())));
    }
}
