package com.eduapp.backend.controller;

import com.eduapp.backend.model.AIAnalysis;
import com.eduapp.backend.service.AIAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ai-analyses")
public class AIAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisController.class);

    private final AIAnalysisService aiAnalysisService;

    public AIAnalysisController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping
    public ResponseEntity<List<AIAnalysis>> getAll() {
        logger.info("Fetching all AI analyses");
        List<AIAnalysis> analyses = aiAnalysisService.findAll();
        return ResponseEntity.ok(analyses);
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