package com.eduapp.backend.controller;

import com.eduapp.backend.model.Progress;
import com.eduapp.backend.service.ProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<Progress>> getAll() {
        logger.info("Fetching all progress");
        List<Progress> progresses = progressService.findAll();
        return ResponseEntity.ok(progresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Progress> getById(@PathVariable Long id) {
        logger.info("Fetching progress with ID: {}", id);
        Optional<Progress> progress = progressService.findById(id);
        return progress.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Progress> create(@RequestBody Progress progress) {
        logger.info("Creating progress");
        Progress saved = progressService.save(progress);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Progress> update(@PathVariable Long id, @RequestBody Progress progress) {
        logger.info("Updating progress with ID: {}", id);
        if (!progressService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        progress.setId(id);
        Progress updated = progressService.save(progress);
        return ResponseEntity.ok(updated);
    }

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