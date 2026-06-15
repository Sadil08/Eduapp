package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.model.Progress;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.service.ProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private static final Logger logger = LoggerFactory.getLogger(ProgressController.class);

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    // N-SC2-A: returns ONLY the authenticated caller's progress. Previously returned every
    // user's progress to any logged-in user (the client-supplied ?userId was ignored).
    @GetMapping
    public ResponseEntity<List<Progress>> getMyProgress(@AuthenticationPrincipal AuthenticatedUser principal) {
        List<Progress> progresses = progressService.findByUserId(principal.getUserId());
        return ResponseEntity.ok(progresses);
    }

    // N-S9: progress is system-written during attempts. Reads-by-id and all writes are
    // ADMIN-only (no non-admin caller); users see their own via the scoped list above.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Progress> getById(@PathVariable Long id) {
        logger.info("Fetching progress with ID: {}", id);
        Optional<Progress> progress = progressService.findById(id);
        return progress.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Progress> create(@Valid @RequestBody Progress progress) {
        logger.info("Creating progress");
        Progress saved = progressService.save(progress);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Progress> update(@PathVariable Long id, @Valid @RequestBody Progress progress) {
        logger.info("Updating progress with ID: {}", id);
        if (!progressService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        progress.setId(id);
        Progress updated = progressService.save(progress);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting progress with ID: {}", id);
        if (!progressService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        progressService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}