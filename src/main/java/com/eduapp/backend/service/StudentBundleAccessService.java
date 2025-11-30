package com.eduapp.backend.service;

import com.eduapp.backend.dto.MyBundleDto;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.repository.StudentBundleAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<StudentBundleAccess> findByStudentId(Long studentId) {
        logger.info("Fetching bundle accesses for student ID: {}", studentId);
        return studentBundleAccessRepository.findByStudentId(studentId);
    }

    /**
     * Converts StudentBundleAccess entities to MyBundleDto for dashboard display
     * This avoids circular references and sensitive data exposure
     */
    public List<MyBundleDto> getMyBundlesDto(Long studentId) {
        logger.info("Fetching bundles as DTOs for student ID: {}", studentId);
        List<StudentBundleAccess> accesses = studentBundleAccessRepository.findByStudentId(studentId);

        return accesses.stream()
                .map(access -> {
                    PaperBundle bundle = access.getBundle();
                    return new MyBundleDto(
                            access.getId(),
                            bundle.getId(),
                            bundle.getName(),
                            bundle.getDescription(),
                            bundle.getPrice(),
                            bundle.getType(),
                            bundle.getExamType(),
                            bundle.getIsPastPaper(),
                            bundle.getSubject() != null ? bundle.getSubject().getName() : null,
                            bundle.getLesson() != null ? bundle.getLesson().getName() : null,
                            access.getPurchasedAt(),
                            bundle.getPapers() != null ? bundle.getPapers().size() : 0);
                })
                .collect(Collectors.toList());
    }

    public java.math.BigDecimal calculateTotalRevenue() {
        java.math.BigDecimal total = studentBundleAccessRepository.sumPricePaid();
        return total != null ? total : java.math.BigDecimal.ZERO;
    }
}