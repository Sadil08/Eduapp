package com.eduapp.backend.controller;

import com.eduapp.backend.dto.CreateInviteRequest;
import com.eduapp.backend.dto.SchoolInviteDto;
import com.eduapp.backend.security.TenantContext;
import com.eduapp.backend.service.SchoolInviteService;
import com.eduapp.backend.repository.SchoolInviteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Teacher-invite management for school admins. Creation is restricted to SCHOOL_ADMIN
 * and scoped to that admin's tenant.
 */
@RestController
@RequestMapping("/api/school/invites")
@PreAuthorize("hasRole('SCHOOL_ADMIN')")
public class SchoolInviteController {

    private final SchoolInviteService inviteService;
    private final SchoolInviteRepository inviteRepository;

    public SchoolInviteController(SchoolInviteService inviteService, SchoolInviteRepository inviteRepository) {
        this.inviteService = inviteService;
        this.inviteRepository = inviteRepository;
    }

    @PostMapping
    public ResponseEntity<SchoolInviteDto> create(@RequestBody CreateInviteRequest req) {
        return ResponseEntity.ok(SchoolInviteDto.from(inviteService.createTeacherInvite(req.getEmail())));
    }

    @GetMapping
    public ResponseEntity<List<SchoolInviteDto>> list() {
        List<SchoolInviteDto> invites = inviteRepository.findBySchoolId(TenantContext.getSchoolId()).stream()
                .map(SchoolInviteDto::from)
                .toList();
        return ResponseEntity.ok(invites);
    }
}
