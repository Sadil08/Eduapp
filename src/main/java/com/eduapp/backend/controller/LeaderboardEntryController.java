package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.model.LeaderboardEntry;
import com.eduapp.backend.service.LeaderboardEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// N-S9: raw entity-CRUD scaffolding. The user-facing leaderboard is served by
// LeaderboardController (/api/leaderboard/...); this controller has no non-admin callers,
// so it is locked to ADMIN to close the open create/update/delete + mass-assignment holes.
@RestController
@RequestMapping("/api/leaderboard-entries")
@PreAuthorize("hasRole('ADMIN')")
public class LeaderboardEntryController {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardEntryController.class);

    private final LeaderboardEntryService leaderboardEntryService;

    public LeaderboardEntryController(LeaderboardEntryService leaderboardEntryService) {
        this.leaderboardEntryService = leaderboardEntryService;
    }

    // SCALE-4 + data-exposure: this dumped every leaderboard entry to any authenticated
    // caller (and is not used by the UI — the real leaderboard is /api/leaderboard/paper/{id}).
    // Now ADMIN-only and paginated (size cap 100).
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<LeaderboardEntry>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(leaderboardEntryService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaderboardEntry> getById(@PathVariable Long id) {
        logger.info("Fetching leaderboard entry with ID: {}", id);
        Optional<LeaderboardEntry> entry = leaderboardEntryService.findById(id);
        return entry.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LeaderboardEntry> create(@Valid @RequestBody LeaderboardEntry entry) {
        logger.info("Creating leaderboard entry");
        LeaderboardEntry saved = leaderboardEntryService.save(entry);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaderboardEntry> update(@PathVariable Long id, @Valid @RequestBody LeaderboardEntry entry) {
        logger.info("Updating leaderboard entry with ID: {}", id);
        if (!leaderboardEntryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entry.setId(id);
        LeaderboardEntry updated = leaderboardEntryService.save(entry);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting leaderboard entry with ID: {}", id);
        if (!leaderboardEntryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        leaderboardEntryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}