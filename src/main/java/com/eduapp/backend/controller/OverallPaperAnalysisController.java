package com.eduapp.backend.controller;

import com.eduapp.backend.model.OverallPaperAnalysis;
import com.eduapp.backend.service.OverallPaperAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/overall-paper-analyses")
public class OverallPaperAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(OverallPaperAnalysisController.class);

    private final OverallPaperAnalysisService overallPaperAnalysisService;

    public OverallPaperAnalysisController(OverallPaperAnalysisService overallPaperAnalysisService) {
        this.overallPaperAnalysisService = overallPaperAnalysisService;
    }

    @GetMapping
    public ResponseEntity<List<OverallPaperAnalysis>> getAll() {
        logger.info("Fetching all overall paper analyses");
        List<OverallPaperAnalysis> analyses = overallPaperAnalysisService.findAll();
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OverallPaperAnalysis> getById(@PathVariable Long id) {
        logger.info("Fetching overall paper analysis with ID: {}", id);
        Optional<OverallPaperAnalysis> analysis = overallPaperAnalysisService.findById(id);
        return analysis.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OverallPaperAnalysis> create(@RequestBody OverallPaperAnalysis analysis) {
        logger.info("Creating overall paper analysis");
        OverallPaperAnalysis saved = overallPaperAnalysisService.save(analysis);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OverallPaperAnalysis> update(@PathVariable Long id, @RequestBody OverallPaperAnalysis analysis) {
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