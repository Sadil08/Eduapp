package com.eduapp.backend.controller;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.SchoolPaperService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * School-tier paper management for teachers/admins (WP-6). All operations are
 * tenant-scoped by {@link SchoolPaperService} via the request's TenantContext.
 */
@RestController
@RequestMapping("/api/school/papers")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
public class SchoolPaperController {

    private final SchoolPaperService paperService;
    private final UserRepository userRepository;

    public SchoolPaperController(SchoolPaperService paperService, UserRepository userRepository) {
        this.paperService = paperService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SchoolPaperDto> create(@AuthenticationPrincipal UserDetails principal,
            @RequestBody CreateSchoolPaperRequest req) {
        Long createdBy = userRepository.findByEmail(principal.getUsername()).map(User::getId).orElse(null);
        return ResponseEntity.ok(SchoolPaperDto.from(
                paperService.createDraft(req.getName(), req.getDescription(), req.getType(), createdBy)));
    }

    @GetMapping
    public ResponseEntity<List<SchoolPaperDto>> list() {
        return ResponseEntity.ok(paperService.listForTenant().stream().map(SchoolPaperDto::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolPaperDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(SchoolPaperDto.from(paperService.getForTenant(id)));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<SchoolQuestionDto>> questions(@PathVariable Long id) {
        return ResponseEntity.ok(paperService.listQuestions(id).stream().map(SchoolQuestionDto::from).toList());
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<SchoolQuestionDto> addQuestion(@PathVariable Long id,
            @RequestBody AddSchoolQuestionRequest req) {
        return ResponseEntity.ok(SchoolQuestionDto.from(
                paperService.addQuestion(id, req.getText(), req.getType(), req.getMarks(), req.getCorrectAnswerText())));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<SchoolPaperDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(SchoolPaperDto.from(paperService.approve(id)));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<SchoolPaperDto> assign(@PathVariable Long id, @RequestBody AssignSchoolPaperRequest req) {
        return ResponseEntity.ok(SchoolPaperDto.from(paperService.assignToClass(
                id, req.getClassId(), req.getExamWindowStart(), req.getExamWindowEnd(), req.getTimeLimitMinutes())));
    }

    @PostMapping("/{id}/release-results")
    public ResponseEntity<SchoolPaperDto> releaseResults(@PathVariable Long id) {
        return ResponseEntity.ok(SchoolPaperDto.from(paperService.releaseResults(id)));
    }
}
