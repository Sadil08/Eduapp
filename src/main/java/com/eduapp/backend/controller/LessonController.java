package com.eduapp.backend.controller;

import com.eduapp.backend.dto.LessonDto;
import com.eduapp.backend.mapper.LessonMapper;
import com.eduapp.backend.model.Lesson;
import com.eduapp.backend.service.LessonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Lesson entities.
 * Provides RESTful endpoints for CRUD operations on lessons.
 */
@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private static final Logger logger = LoggerFactory.getLogger(LessonController.class);

    private final LessonService lessonService;
    private final LessonMapper lessonMapper;

    public LessonController(LessonService lessonService, LessonMapper lessonMapper) {
        this.lessonService = lessonService;
        this.lessonMapper = lessonMapper;
    }

    @GetMapping
    public ResponseEntity<List<LessonDto>> getAllLessons() {
        logger.info("Received request to get all lessons");
        List<Lesson> lessons = lessonService.findAll();
        List<LessonDto> dtos = lessonMapper.toDtoList(lessons);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> getLessonById(@PathVariable Long id) {
        logger.info("Received request to get lesson with ID: {}", id);
        Optional<Lesson> lesson = lessonService.findById(id);
        if (lesson.isPresent()) {
            LessonDto dto = lessonMapper.toDto(lesson.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<LessonDto> createLesson(@RequestBody LessonDto dto) {
        logger.info("Received request to create lesson: {}", dto.getName());
        Lesson lesson = lessonMapper.toEntity(dto);
        Lesson savedLesson = lessonService.save(lesson);
        LessonDto savedDto = lessonMapper.toDto(savedLesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonDto> updateLesson(@PathVariable Long id, @RequestBody LessonDto dto) {
        logger.info("Received request to update lesson with ID: {}", id);
        Optional<Lesson> existingLesson = lessonService.findById(id);
        if (existingLesson.isPresent()) {
            Lesson lesson = existingLesson.get();
            lesson.setName(dto.getName());
            lesson.setDescription(dto.getDescription());
            Lesson updatedLesson = lessonService.save(lesson);
            LessonDto updatedDto = lessonMapper.toDto(updatedLesson);
            return ResponseEntity.ok(updatedDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        logger.info("Received request to delete lesson with ID: {}", id);
        if (lessonService.existsById(id)) {
            lessonService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}