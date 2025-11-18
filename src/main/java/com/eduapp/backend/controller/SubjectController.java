package com.eduapp.backend.controller;

import com.eduapp.backend.dto.SubjectDto;
import com.eduapp.backend.mapper.SubjectMapper;
import com.eduapp.backend.model.Subject;
import com.eduapp.backend.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Subject entities.
 * Provides RESTful endpoints for CRUD operations on subjects.
 * Follows REST conventions with appropriate HTTP methods and status codes.
 */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private static final Logger logger = LoggerFactory.getLogger(SubjectController.class);

    private final SubjectService subjectService;
    private final SubjectMapper subjectMapper;

    /**
     * Constructor for dependency injection of service and mapper.
     * @param subjectService the service for Subject operations
     * @param subjectMapper the mapper for Subject DTOs
     */
    public SubjectController(SubjectService subjectService, SubjectMapper subjectMapper) {
        this.subjectService = subjectService;
        this.subjectMapper = subjectMapper;
    }

    /**
     * Retrieves all subjects.
     * @return a ResponseEntity containing a list of SubjectDto
     */
    @GetMapping
    public ResponseEntity<List<SubjectDto>> getAllSubjects() {
        logger.info("Received request to get all subjects");
        List<Subject> subjects = subjectService.findAll();
        List<SubjectDto> dtos = subjectMapper.toDtoList(subjects);
        logger.info("Returning {} subjects", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Retrieves a subject by its ID.
     * @param id the ID of the subject
     * @return a ResponseEntity containing the SubjectDto if found, or 404 if not
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubjectDto> getSubjectById(@PathVariable Long id) {
        logger.info("Received request to get subject with ID: {}", id);
        Optional<Subject> subject = subjectService.findById(id);
        if (subject.isPresent()) {
            SubjectDto dto = subjectMapper.toDto(subject.get());
            logger.info("Subject found and returned");
            return ResponseEntity.ok(dto);
        } else {
            logger.warn("Subject with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Creates a new subject.
     * @param dto the SubjectDto containing subject data
     * @return a ResponseEntity containing the created SubjectDto
     */
    @PostMapping
    public ResponseEntity<SubjectDto> createSubject(@RequestBody SubjectDto dto) {
        logger.info("Received request to create subject: {}", dto.getName());
        Subject subject = subjectMapper.toEntity(dto);
        Subject savedSubject = subjectService.save(subject);
        SubjectDto savedDto = subjectMapper.toDto(savedSubject);
        logger.info("Subject created with ID: {}", savedDto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    /**
     * Updates an existing subject.
     * @param id the ID of the subject to update
     * @param dto the SubjectDto containing updated data
     * @return a ResponseEntity containing the updated SubjectDto if found, or 404 if not
     */
    @PutMapping("/{id}")
    public ResponseEntity<SubjectDto> updateSubject(@PathVariable Long id, @RequestBody SubjectDto dto) {
        logger.info("Received request to update subject with ID: {}", id);
        Optional<Subject> existingSubject = subjectService.findById(id);
        if (existingSubject.isPresent()) {
            Subject subject = existingSubject.get();
            // Update fields
            subject.setName(dto.getName());
            Subject updatedSubject = subjectService.save(subject);
            SubjectDto updatedDto = subjectMapper.toDto(updatedSubject);
            logger.info("Subject updated");
            return ResponseEntity.ok(updatedDto);
        } else {
            logger.warn("Subject with ID {} not found for update", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a subject by its ID.
     * @param id the ID of the subject to delete
     * @return a ResponseEntity with 204 if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        logger.info("Received request to delete subject with ID: {}", id);
        if (subjectService.existsById(id)) {
            subjectService.deleteById(id);
            logger.info("Subject deleted");
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Subject with ID {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        }
    }
}