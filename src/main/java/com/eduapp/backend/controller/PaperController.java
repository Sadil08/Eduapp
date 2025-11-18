package com.eduapp.backend.controller;

import com.eduapp.backend.dto.PaperDto;
import com.eduapp.backend.mapper.PaperMapper;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperType;
import com.eduapp.backend.service.PaperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Paper entities.
 * Provides RESTful endpoints for CRUD operations on papers.
 */
@RestController
@RequestMapping("/api/papers")
public class PaperController {

    private static final Logger logger = LoggerFactory.getLogger(PaperController.class);

    private final PaperService paperService;
    private final PaperMapper paperMapper;

    public PaperController(PaperService paperService, PaperMapper paperMapper) {
        this.paperService = paperService;
        this.paperMapper = paperMapper;
    }

    @GetMapping
    public ResponseEntity<List<PaperDto>> getAllPapers() {
        logger.info("Received request to get all papers");
        List<Paper> papers = paperService.findAll();
        List<PaperDto> dtos = paperMapper.toDtoList(papers);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaperDto> getPaperById(@PathVariable Long id) {
        logger.info("Received request to get paper with ID: {}", id);
        Optional<Paper> paper = paperService.findById(id);
        if (paper.isPresent()) {
            PaperDto dto = paperMapper.toDto(paper.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<PaperDto> createPaper(@RequestBody PaperDto dto) {
        logger.info("Received request to create paper: {}", dto.getName());
        Paper paper = paperMapper.toEntity(dto);
        Paper savedPaper = paperService.save(paper);
        PaperDto savedDto = paperMapper.toDto(savedPaper);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaperDto> updatePaper(@PathVariable Long id, @RequestBody PaperDto dto) {
        logger.info("Received request to update paper with ID: {}", id);
        Optional<Paper> existingPaper = paperService.findById(id);
        if (existingPaper.isPresent()) {
            Paper paper = existingPaper.get();
            paper.setName(dto.getName());
            paper.setDescription(dto.getDescription());
            paper.setType(dto.getType());
            paper.setMaxFreeAttempts(dto.getMaxFreeAttempts());
            Paper updatedPaper = paperService.save(paper);
            PaperDto updatedDto = paperMapper.toDto(updatedPaper);
            return ResponseEntity.ok(updatedDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaper(@PathVariable Long id) {
        logger.info("Received request to delete paper with ID: {}", id);
        if (paperService.existsById(id)) {
            paperService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}