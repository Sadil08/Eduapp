package com.eduapp.backend.controller;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.SchoolPaperAttempt;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.SchoolAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Student exam-taking (WP-7). Sitting is constrained to the paper's exam window and to
 * the student's own enrolment. Results are visible only once the teacher releases them.
 */
@RestController
@RequestMapping("/api/student/attempts")
@PreAuthorize("hasRole('SCHOOL_STUDENT')")
public class StudentAttemptController {

    private final SchoolAttemptService attemptService;
    private final UserRepository userRepository;

    public StudentAttemptController(SchoolAttemptService attemptService, UserRepository userRepository) {
        this.attemptService = attemptService;
        this.userRepository = userRepository;
    }

    private Long currentUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping("/start")
    public ResponseEntity<SchoolAttemptDto> start(@AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, Long> body) {
        SchoolPaperAttempt attempt = attemptService.start(currentUserId(principal), body.get("paperId"));
        return ResponseEntity.ok(SchoolAttemptDto.from(attempt));
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<Void> answer(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id, @RequestBody SubmitAnswerRequest req) {
        attemptService.answer(currentUserId(principal), id, req.getQuestionId(), req.getAnswerText());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SchoolAttemptDto> submit(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(SchoolAttemptDto.from(attemptService.submit(currentUserId(principal), id)));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<StudentAttemptResultDto> result(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        SchoolPaperAttempt attempt = attemptService.getOwnAttempt(currentUserId(principal), id);
        boolean released = attemptService.areResultsReleased(attempt.getSchoolPaperId());
        return ResponseEntity.ok(StudentAttemptResultDto.of(attempt, released));
    }
}
