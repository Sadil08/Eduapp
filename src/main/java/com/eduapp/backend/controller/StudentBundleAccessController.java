package com.eduapp.backend.controller;

import com.eduapp.backend.dto.MyBundleDto;
import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.service.StudentBundleAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.eduapp.backend.security.JwtUtil;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/student-bundle-accesses")
public class StudentBundleAccessController {

    private static final Logger logger = LoggerFactory.getLogger(StudentBundleAccessController.class);

    private final StudentBundleAccessService studentBundleAccessService;
    private final JwtUtil jwtUtil;

    public StudentBundleAccessController(StudentBundleAccessService studentBundleAccessService, JwtUtil jwtUtil) {
        this.studentBundleAccessService = studentBundleAccessService;
        this.jwtUtil = jwtUtil;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<StudentBundleAccess>> getAll() {
        logger.info("Fetching all student bundle accesses");
        List<StudentBundleAccess> accesses = studentBundleAccessService.findAll();
        return ResponseEntity.ok(accesses);
    }

    @GetMapping("/my-bundles")
    public ResponseEntity<List<MyBundleDto>> getMyBundles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        logger.info("Fetching bundles for user ID: {}", userId);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<MyBundleDto> bundles = studentBundleAccessService.getMyBundlesDto(userId);
        return ResponseEntity.ok(bundles);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<StudentBundleAccess> getById(@PathVariable Long id) {
        logger.info("Fetching student bundle access with ID: {}", id);
        Optional<StudentBundleAccess> access = studentBundleAccessService.findById(id);
        return access.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StudentBundleAccess> create(@RequestBody StudentBundleAccess access) {
        logger.info("Creating student bundle access");
        StudentBundleAccess saved = studentBundleAccessService.save(access);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<StudentBundleAccess> update(@PathVariable Long id, @RequestBody StudentBundleAccess access) {
        logger.info("Updating student bundle access with ID: {}", id);
        if (!studentBundleAccessService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        access.setId(id);
        StudentBundleAccess updated = studentBundleAccessService.save(access);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting student bundle access with ID: {}", id);
        if (!studentBundleAccessService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        studentBundleAccessService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}