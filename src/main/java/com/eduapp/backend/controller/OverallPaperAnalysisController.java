package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.model.OverallPaperAnalysis;
import com.eduapp.backend.service.OverallPaperAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// N-SC2-A: admin-only management tooling (no public/student callers).
@RestController
@RequestMapping("/api/overall-paper-analyses")
@PreAuthorize("hasRole('ADMIN')")
public class OverallPaperAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(OverallPaperAnalysisController.class);

    private final OverallPaperAnalysisService overallPaperAnalysisService;

    public OverallPaperAnalysisController(OverallPaperAnalysisService overallPaperAnalysisService) {
        this.overallPaperAnalysisService = overallPaperAnalysisService;
    }

    // N-SC2: ADMIN-only (class-level) + paginated. Returns a Spring Page.
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<OverallPaperAnalysis>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = org.springframework.data.domain.PageRequest.of(Math.max(page, 0), safeSize);
        return ResponseEntity.ok(overallPaperAnalysisService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OverallPaperAnalysis> getById(@PathVariable Long id) {
        logger.info("Fetching overall paper analysis with ID: {}", id);
        Optional<OverallPaperAnalysis> analysis = overallPaperAnalysisService.findById(id);
        return analysis.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OverallPaperAnalysis> create(@Valid @RequestBody OverallPaperAnalysis analysis) {
        logger.info("Creating overall paper analysis");
        OverallPaperAnalysis saved = overallPaperAnalysisService.save(analysis);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OverallPaperAnalysis> update(@PathVariable Long id, @Valid @RequestBody OverallPaperAnalysis analysis) {
        logger.info("Updating overall paper analysis with ID: {}", id);
        if (!overallPaperAnalysisService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        analysis.setId(id);
        OverallPaperAnalysis updated = overallPaperAnalysisService.save(analysis);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting overall paper analysis with ID: {}", id);
        if (!overallPaperAnalysisService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        overallPaperAnalysisService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}