package com.eduapp.backend.controller;

import jakarta.validation.Valid;

import com.eduapp.backend.dto.QuestionOptionDto;
import com.eduapp.backend.mapper.QuestionOptionMapper;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.service.QuestionOptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for managing QuestionOption entities.
 * N-SC2-A: admin-only management tooling (no public/student callers). Options reach students
 * embedded in question/paper DTOs, not via this controller.
 */
@RestController
@RequestMapping("/api/question-options")
@PreAuthorize("hasRole('ADMIN')")
public class QuestionOptionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionOptionController.class);

    private final QuestionOptionService questionOptionService;
    private final QuestionOptionMapper questionOptionMapper;

    public QuestionOptionController(QuestionOptionService questionOptionService, QuestionOptionMapper questionOptionMapper) {
        this.questionOptionService = questionOptionService;
        this.questionOptionMapper = questionOptionMapper;
    }

    // N-SC2: ADMIN-only (class-level) + paginated. Returns a Spring Page.
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<QuestionOptionDto>> getAllQuestionOptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = org.springframework.data.domain.PageRequest.of(Math.max(page, 0), safeSize);
        return ResponseEntity.ok(questionOptionService.findAll(pageable).map(questionOptionMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionOptionDto> getQuestionOptionById(@PathVariable Long id) {
        logger.info("Received request to get question option with ID: {}", id);
        Optional<QuestionOption> option = questionOptionService.findById(id);
        if (option.isPresent()) {
            QuestionOptionDto dto = questionOptionMapper.toDto(option.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<QuestionOptionDto> createQuestionOption(@Valid @RequestBody QuestionOptionDto dto) {
        logger.info("Received request to create question option");
        QuestionOption option = questionOptionMapper.toEntity(dto);
        QuestionOption savedOption = questionOptionService.save(option);
        QuestionOptionDto savedDto = questionOptionMapper.toDto(savedOption);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionOption(@PathVariable Long id) {
        logger.info("Received request to delete question option with ID: {}", id);
        if (questionOptionService.existsById(id)) {
            questionOptionService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}