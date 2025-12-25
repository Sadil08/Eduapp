package com.eduapp.backend.controller;

import com.eduapp.backend.model.ExamType;
import com.eduapp.backend.service.ExamTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exam-types")
public class ExamTypeController {

    private final ExamTypeService examTypeService;

    public ExamTypeController(ExamTypeService examTypeService) {
        this.examTypeService = examTypeService;
    }

    @GetMapping
    public ResponseEntity<List<ExamType>> getAllExamTypes() {
        return ResponseEntity.ok(examTypeService.getAllExamTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamType> getExamTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(examTypeService.getExamTypeById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ExamType> createExamType(@RequestBody ExamType examType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examTypeService.createExamType(examType));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ExamType> updateExamType(@PathVariable Long id, @RequestBody ExamType examType) {
        return ResponseEntity.ok(examTypeService.updateExamType(id, examType));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExamType(@PathVariable Long id) {
        examTypeService.deleteExamType(id);
        return ResponseEntity.noContent().build();
    }
}
