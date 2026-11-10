package com.eduapp.backend.controller;

import com.eduapp.backend.dto.SchoolPaperDto;
import com.eduapp.backend.dto.StudentQuestionDto;
import com.eduapp.backend.model.SchoolPaper;
import com.eduapp.backend.model.SchoolPaperStatus;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.SchoolEnrolmentRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.SchoolPaperService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Student-facing view of school papers (WP-6). Outside /api/school/** (which is
 * teacher/admin only). A student only ever sees ASSIGNED papers, and only for a class
 * they are enrolled in — draft/approved papers are invisible.
 */
@RestController
@RequestMapping("/api/student/papers")
@PreAuthorize("hasRole('SCHOOL_STUDENT')")
public class StudentPaperViewController {

    private final SchoolPaperService paperService;
    private final SchoolEnrolmentRepository enrolmentRepository;
    private final UserRepository userRepository;

    public StudentPaperViewController(SchoolPaperService paperService,
            SchoolEnrolmentRepository enrolmentRepository,
            UserRepository userRepository) {
        this.paperService = paperService;
        this.enrolmentRepository = enrolmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<SchoolPaperDto>> assignedForClass(@AuthenticationPrincipal UserDetails principal,
            @RequestParam Long classId) {
        User student = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // BT-9.5: only an ACTIVE (teacher-approved) enrolment grants class access.
        if (!enrolmentRepository.existsByClassIdAndStudentIdAndStatus(
                classId, student.getId(), com.eduapp.backend.model.SchoolEnrolmentStatus.ACTIVE)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Your enrolment in this class is not active yet");
        }
        return ResponseEntity.ok(paperService.listAssignedForClass(classId).stream()
                .map(SchoolPaperDto::from).toList());
    }

    /** Metadata of an ASSIGNED paper (exam window, time limit) for the attempt screen. */
    @GetMapping("/{paperId}")
    public ResponseEntity<SchoolPaperDto> paper(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long paperId) {
        User student = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        SchoolPaper paper = paperService.getForTenant(paperId);
        if (paper.getStatus() != SchoolPaperStatus.ASSIGNED || paper.getClassId() == null
                || !enrolmentRepository.existsByClassIdAndStudentIdAndStatus(
                        paper.getClassId(), student.getId(), com.eduapp.backend.model.SchoolEnrolmentStatus.ACTIVE)) {
            throw new org.springframework.security.access.AccessDeniedException("This paper is not available to you");
        }
        return ResponseEntity.ok(SchoolPaperDto.from(paper));
    }

    /**
     * Questions of an ASSIGNED paper, for attempting — WITHOUT the marking scheme. Requires an
     * ACTIVE enrolment in the paper's class (BT-9.5).
     */
    @GetMapping("/{paperId}/questions")
    public ResponseEntity<List<StudentQuestionDto>> questions(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long paperId) {
        User student = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        SchoolPaper paper = paperService.getForTenant(paperId);
        if (paper.getStatus() != SchoolPaperStatus.ASSIGNED || paper.getClassId() == null
                || !enrolmentRepository.existsByClassIdAndStudentIdAndStatus(
                        paper.getClassId(), student.getId(), com.eduapp.backend.model.SchoolEnrolmentStatus.ACTIVE)) {
            throw new org.springframework.security.access.AccessDeniedException("This paper is not available to you");
        }
        return ResponseEntity.ok(paperService.listStudentQuestionDtos(paperId));
    }
}
