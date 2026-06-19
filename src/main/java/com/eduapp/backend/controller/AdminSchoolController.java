package com.eduapp.backend.controller;

import com.eduapp.backend.dto.SchoolCreateRequest;
import com.eduapp.backend.dto.SchoolDto;
import com.eduapp.backend.model.School;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.service.SchoolService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** BT-1: ADMIN-only school onboarding + listing. */
@RestController
@RequestMapping("/api/admin/schools")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSchoolController {

    private static final int MAX_PAGE_SIZE = 100;

    private final SchoolService schoolService;

    public AdminSchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @PostMapping
    public ResponseEntity<SchoolDto> create(@AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody SchoolCreateRequest req) {
        School created = schoolService.createSchool(req, principal.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(SchoolDto.from(created));
    }

    @GetMapping
    public ResponseEntity<Page<SchoolDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Page<SchoolDto> result = schoolService
                .list(PageRequest.of(Math.max(page, 0), safeSize))
                .map(SchoolDto::from);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(SchoolDto.from(schoolService.get(id)));
    }

    /** Resend the set-password OTP to the school's admin(s) (timed-out / fresh token). */
    @PostMapping("/{id}/resend-admin-invite")
    public ResponseEntity<java.util.Map<String, Object>> resendAdminInvite(@PathVariable Long id) {
        java.util.List<String> emails = schoolService.resendAdminInvite(id);
        return ResponseEntity.ok(java.util.Map.of("emails", emails, "count", emails.size()));
    }
}
