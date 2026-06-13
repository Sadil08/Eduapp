package com.eduapp.backend.controller;

import com.eduapp.backend.dto.SchoolClassDto;
import com.eduapp.backend.service.SchoolClassService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * School-tier class management (WP-5). Tenant scoping is enforced entirely by
 * {@link SchoolClassService} via the request's {@code TenantContext} — no school id
 * is accepted from the client.
 */
@RestController
@RequestMapping("/api/school/classes")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER')")
public class SchoolClassController {

    private final SchoolClassService service;

    public SchoolClassController(SchoolClassService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SchoolClassDto>> list() {
        List<SchoolClassDto> classes = service.listForCurrentTenant().stream()
                .map(SchoolClassDto::from)
                .toList();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolClassDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(SchoolClassDto.from(service.getForCurrentTenant(id)));
    }

    @PostMapping
    public ResponseEntity<SchoolClassDto> create(@RequestBody SchoolClassDto req) {
        return ResponseEntity.ok(SchoolClassDto.from(
                service.create(req.getTeacherId(), req.getName(), req.getSubject(), req.getYearGroup())));
    }
}
