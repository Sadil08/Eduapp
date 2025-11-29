package com.eduapp.backend.controller;

import com.eduapp.backend.model.LeaderboardEntry;
import com.eduapp.backend.service.LeaderboardEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/leaderboard-entries")
public class LeaderboardEntryController {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardEntryController.class);

    private final LeaderboardEntryService leaderboardEntryService;

    public LeaderboardEntryController(LeaderboardEntryService leaderboardEntryService) {
        this.leaderboardEntryService = leaderboardEntryService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderboardEntry>> getAll() {
        logger.info("Fetching all leaderboard entries");
        List<LeaderboardEntry> entries = leaderboardEntryService.findAll();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaderboardEntry> getById(@PathVariable Long id) {
        logger.info("Fetching leaderboard entry with ID: {}", id);
        Optional<LeaderboardEntry> entry = leaderboardEntryService.findById(id);
        return entry.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LeaderboardEntry> create(@RequestBody LeaderboardEntry entry) {
        logger.info("Creating leaderboard entry");
        LeaderboardEntry saved = leaderboardEntryService.save(entry);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaderboardEntry> update(@PathVariable Long id, @RequestBody LeaderboardEntry entry) {
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