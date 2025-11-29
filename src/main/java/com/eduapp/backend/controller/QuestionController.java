package com.eduapp.backend.controller;

import com.eduapp.backend.dto.QuestionDto;
import com.eduapp.backend.mapper.QuestionMapper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Question entities.
 * Provides RESTful endpoints for CRUD operations on questions.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    private final QuestionService questionService;
    private final QuestionMapper questionMapper;

    public QuestionController(QuestionService questionService, QuestionMapper questionMapper) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDto>> getAllQuestions() {
        logger.info("Received request to get all questions");
        List<Question> questions = questionService.findAll();
        List<QuestionDto> dtos = questionMapper.toDtoList(questions);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Long id) {
        logger.info("Received request to get question with ID: {}", id);
        Optional<Question> question = questionService.findById(id);
        if (question.isPresent()) {
            QuestionDto dto = questionMapper.toDto(question.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<QuestionDto> createQuestion(@RequestBody QuestionDto dto) {
        logger.info("Received request to create question");
        Question question = questionMapper.toEntity(dto);
        Question savedQuestion = questionService.save(question);
        QuestionDto savedDto = questionMapper.toDto(savedQuestion);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(@PathVariable Long id, @RequestBody QuestionDto dto) {
        logger.info("Received request to update question with ID: {}", id);
        Optional<Question> existingQuestion = questionService.findById(id);
        if (existingQuestion.isPresent()) {
            Question question = existingQuestion.get();
            question.setText(dto.getText());
            question.setCorrectAnswerText(dto.getCorrectAnswerText());
            Question updatedQuestion = questionService.save(question);
            QuestionDto updatedDto = questionMapper.toDto(updatedQuestion);
            return ResponseEntity.ok(updatedDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        logger.info("Received request to delete question with ID: {}", id);
        if (questionService.existsById(id)) {
            questionService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}