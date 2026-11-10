package com.eduapp.backend.controller;

import jakarta.validation.Valid;

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
            @Valid @RequestBody CreateSchoolPaperRequest req) {
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

    @PutMapping("/{id}")
    public ResponseEntity<SchoolPaperDto> update(@PathVariable Long id,
            @Valid @RequestBody UpdateSchoolPaperRequest req) {
        return ResponseEntity.ok(SchoolPaperDto.from(
                paperService.updatePaper(id, req.getName(), req.getDescription())));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<SchoolQuestionDto>> questions(@PathVariable Long id) {
        return ResponseEntity.ok(paperService.listQuestionDtos(id));
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<SchoolQuestionDto> addQuestion(@PathVariable Long id,
            @Valid @RequestBody AddSchoolQuestionRequest req) {
        return ResponseEntity.ok(paperService.addQuestion(
                id, req.getText(), req.getType(), req.getMarks(), req.getCorrectAnswerText(),
                req.getImageUrl(), req.getModelAnswerImageUrl(), req.getOptions()));
    }

    @PutMapping("/{id}/questions/{questionId}")
    public ResponseEntity<SchoolQuestionDto> updateQuestion(@PathVariable Long id, @PathVariable Long questionId,
            @Valid @RequestBody AddSchoolQuestionRequest req) {
        return ResponseEntity.ok(paperService.updateQuestion(
                id, questionId, req.getText(), req.getType(), req.getMarks(), req.getCorrectAnswerText(),
                req.getImageUrl(), req.getModelAnswerImageUrl(), req.getOptions()));
    }

    /**
     * Upload a question or model-answer image → stored + AI-extracted (reuses the global
     * pipeline). Returns {imageUrl, extractedText}; the client decides where to place the text.
     * docType: "question" (default) or "answer".
     */
    @PostMapping(value = "/{id}/extract", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> extract(@PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "docType", required = false) String docType) {
        return ResponseEntity.ok(paperService.extractImage(id, file, docType));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        paperService.deleteQuestion(id, questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<SchoolPaperDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(SchoolPaperDto.from(paperService.approve(id)));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<SchoolPaperDto> assign(@PathVariable Long id, @Valid @RequestBody AssignSchoolPaperRequest req) {
        return ResponseEntity.ok(SchoolPaperDto.from(paperService.assignToClass(
                id, req.getClassId(), req.getExamWindowStart(), req.getExamWindowEnd(), req.getTimeLimitMinutes())));
    }

    @PostMapping("/{id}/release-results")
    public ResponseEntity<SchoolPaperDto> releaseResults(@PathVariable Long id) {
        return ResponseEntity.ok(SchoolPaperDto.from(paperService.releaseResults(id)));
    }
}
