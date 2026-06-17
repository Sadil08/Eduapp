package com.eduapp.backend.controller;

import com.eduapp.backend.dto.SchoolEnrolmentDto;
import com.eduapp.backend.model.SchoolEnrolmentStatus;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.service.SchoolEnrolmentReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BT-9: teacher/admin review of class enrolments (pending join requests + direct enrol).
 * A TEACHER may only act on classes they teach; a SCHOOL_ADMIN on any class in the tenant
 * (enforced in {@link SchoolEnrolmentReviewService}).
 */
@RestController
@RequestMapping("/api/school/classes/{classId}/enrolments")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
public class SchoolEnrolmentController {

    private final SchoolEnrolmentReviewService service;

    public SchoolEnrolmentController(SchoolEnrolmentReviewService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SchoolEnrolmentDto>> list(@PathVariable Long classId,
            @RequestParam(required = false) SchoolEnrolmentStatus status,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(service.list(classId, status, caller));
    }

    @PostMapping("/{enrolmentId}/accept")
    public ResponseEntity<SchoolEnrolmentDto> accept(@PathVariable Long classId,
            @PathVariable Long enrolmentId, @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(service.setStatus(classId, enrolmentId, SchoolEnrolmentStatus.ACTIVE, caller));
    }

    @PostMapping("/{enrolmentId}/reject")
    public ResponseEntity<SchoolEnrolmentDto> reject(@PathVariable Long classId,
            @PathVariable Long enrolmentId, @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(service.setStatus(classId, enrolmentId, SchoolEnrolmentStatus.REJECTED, caller));
    }

    /** BT-9.4 / BT-4.3: teacher/admin directly enrols a student (immediately ACTIVE). */
    @PostMapping("/students/{studentId}")
    public ResponseEntity<SchoolEnrolmentDto> directEnrol(@PathVariable Long classId,
            @PathVariable Long studentId, @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(service.directEnrol(classId, studentId, caller));
    }
}
