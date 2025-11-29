package com.eduapp.backend.controller;

import com.eduapp.backend.dto.QuestionOptionDto;
import com.eduapp.backend.mapper.QuestionOptionMapper;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.service.QuestionOptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing QuestionOption entities.
 */
@RestController
@RequestMapping("/api/question-options")
public class QuestionOptionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionOptionController.class);

    private final QuestionOptionService questionOptionService;
    private final QuestionOptionMapper questionOptionMapper;

    public QuestionOptionController(QuestionOptionService questionOptionService, QuestionOptionMapper questionOptionMapper) {
        this.questionOptionService = questionOptionService;
        this.questionOptionMapper = questionOptionMapper;
    }

    @GetMapping
    public ResponseEntity<List<QuestionOptionDto>> getAllQuestionOptions() {
        logger.info("Received request to get all question options");
        List<QuestionOption> options = questionOptionService.findAll();
        List<QuestionOptionDto> dtos = questionOptionMapper.toDtoList(options);
        return ResponseEntity.ok(dtos);
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
    public ResponseEntity<QuestionOptionDto> createQuestionOption(@RequestBody QuestionOptionDto dto) {
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