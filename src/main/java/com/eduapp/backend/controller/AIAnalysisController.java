package com.eduapp.backend.controller;

import com.eduapp.backend.model.AIAnalysis;
import com.eduapp.backend.service.AIAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/ai-analyses")
// SECURITY: this endpoint is NOT under /api/admin/** so it would otherwise be
// reachable by ANY authenticated user. Listing all analyses is admin-only.
@PreAuthorize("hasRole('ADMIN')")
public class AIAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisController.class);

    private final AIAnalysisService aiAnalysisService;

    public AIAnalysisController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    private static final int MAX_PAGE_SIZE = 100;

    // SCALE-3: paginated. Defaults to page 0, size 20; size is capped at 100 server-side.
    @GetMapping
    public ResponseEntity<Page<AIAnalysis>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);
        logger.info("Fetching AI analyses page {} size {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(aiAnalysisService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AIAnalysis> getById(@PathVariable Long id) {
        logger.info("Fetching AI analysis with ID: {}", id);
        Optional<AIAnalysis> analysis = aiAnalysisService.findById(id);
        return analysis.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AIAnalysis> create(@RequestBody AIAnalysis analysis) {
        logger.info("Creating AI analysis");
        AIAnalysis saved = aiAnalysisService.save(analysis);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AIAnalysis> update(@PathVariable Long id, @RequestBody AIAnalysis analysis) {
        logger.info("Updating AI analysis with ID: {}", id);
        if (!aiAnalysisService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        analysis.setId(id);
        AIAnalysis updated = aiAnalysisService.save(analysis);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting AI analysis with ID: {}", id);
        if (!aiAnalysisService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        aiAnalysisService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}