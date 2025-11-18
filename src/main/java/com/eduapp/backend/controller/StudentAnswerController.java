package com.eduapp.backend.controller;

import com.eduapp.backend.dto.StudentAnswerDto;
import com.eduapp.backend.mapper.StudentAnswerMapper;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.service.StudentAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing StudentAnswer entities.
 */
@RestController
@RequestMapping("/api/student-answers")
public class StudentAnswerController {

    private static final Logger logger = LoggerFactory.getLogger(StudentAnswerController.class);

    private final StudentAnswerService studentAnswerService;
    private final StudentAnswerMapper studentAnswerMapper;

    public StudentAnswerController(StudentAnswerService studentAnswerService, StudentAnswerMapper studentAnswerMapper) {
        this.studentAnswerService = studentAnswerService;
        this.studentAnswerMapper = studentAnswerMapper;
    }

    @GetMapping
    public ResponseEntity<List<StudentAnswerDto>> getAllStudentAnswers() {
        logger.info("Received request to get all student answers");
        List<StudentAnswer> answers = studentAnswerService.findAll();
        List<StudentAnswerDto> dtos = studentAnswerMapper.toDtoList(answers);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentAnswerDto> getStudentAnswerById(@PathVariable Long id) {
        logger.info("Received request to get student answer with ID: {}", id);
        Optional<StudentAnswer> answer = studentAnswerService.findById(id);
        if (answer.isPresent()) {
            StudentAnswerDto dto = studentAnswerMapper.toDto(answer.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<StudentAnswerDto> createStudentAnswer(@RequestBody StudentAnswerDto dto) {
        logger.info("Received request to create student answer");
        StudentAnswer answer = studentAnswerMapper.toEntity(dto);
        StudentAnswer savedAnswer = studentAnswerService.save(answer);
        StudentAnswerDto savedDto = studentAnswerMapper.toDto(savedAnswer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentAnswer(@PathVariable Long id) {
        logger.info("Received request to delete student answer with ID: {}", id);
        if (studentAnswerService.existsById(id)) {
            studentAnswerService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}