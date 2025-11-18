package com.eduapp.backend.service;

import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.repository.StudentBundleAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class StudentBundleAccessService {

    private static final Logger logger = LoggerFactory.getLogger(StudentBundleAccessService.class);

    private final StudentBundleAccessRepository studentBundleAccessRepository;

    public StudentBundleAccessService(StudentBundleAccessRepository studentBundleAccessRepository) {
        this.studentBundleAccessRepository = studentBundleAccessRepository;
    }

    public List<StudentBundleAccess> findAll() {
        logger.info("Fetching all student bundle accesses");
        return studentBundleAccessRepository.findAll();
    }

    public Optional<StudentBundleAccess> findById(Long id) {
        logger.info("Fetching student bundle access with ID: {}", id);
        return studentBundleAccessRepository.findById(id);
    }

    public StudentBundleAccess save(StudentBundleAccess studentBundleAccess) {
        logger.info("Saving student bundle access");
        return studentBundleAccessRepository.save(studentBundleAccess);
    }

    public void deleteById(Long id) {
        logger.info("Deleting student bundle access with ID: {}", id);
        studentBundleAccessRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return studentBundleAccessRepository.existsById(id);
    }
}