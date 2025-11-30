package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaperBundleDto;
import com.eduapp.backend.dto.PaperBundleSummaryDto;
import com.eduapp.backend.dto.PaperBundleDetailDto;
import com.eduapp.backend.mapper.PaperBundleMapper;
import com.eduapp.backend.model.Lesson;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.PaperType;
import com.eduapp.backend.model.Subject;
import com.eduapp.backend.model.User;
import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.repository.LessonRepository;
import com.eduapp.backend.repository.PaperBundleRepository;
import com.eduapp.backend.repository.SubjectRepository;
import com.eduapp.backend.repository.StudentBundleAccessRepository;
import com.eduapp.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing PaperBundle entities.
 * Provides business logic for creating and retrieving paper bundles.
 * Follows Single Responsibility Principle by handling only paper bundle-related
 * operations.
 */
@Service
@SuppressWarnings("null")
public class PaperBundleService {

    private static final Logger logger = LoggerFactory.getLogger(PaperBundleService.class);

    private final PaperBundleRepository paperBundleRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final StudentBundleAccessRepository studentBundleAccessRepository;
    private final UserRepository userRepository;
    private final PaperBundleMapper paperBundleMapper;

    /**
     * Constructor for dependency injection of repositories and mapper.
     * 
     * @param paperBundleRepository         the repository for PaperBundle entities
     * @param subjectRepository             the repository for Subject entities
     * @param lessonRepository              the repository for Lesson entities
     * @param studentBundleAccessRepository the repository for StudentBundleAccess
     *                                      entities
     * @param userRepository                the repository for User entities
     * @param paperBundleMapper             the mapper for PaperBundle DTOs
     */
    public PaperBundleService(PaperBundleRepository paperBundleRepository,
            SubjectRepository subjectRepository,
            LessonRepository lessonRepository,
            StudentBundleAccessRepository studentBundleAccessRepository,
            UserRepository userRepository,
            PaperBundleMapper paperBundleMapper) {
        this.paperBundleRepository = paperBundleRepository;
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
        this.studentBundleAccessRepository = studentBundleAccessRepository;
        this.userRepository = userRepository;
        this.paperBundleMapper = paperBundleMapper;
    }

    /**
     * Creates a new paper bundle from the provided DTO.
     * Validates associated entities and saves the bundle.
     * 
     * @param dto the PaperBundleDto containing bundle data
     * @return the created PaperBundleDto
     */
    public PaperBundleDto createBundle(PaperBundleDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PaperBundleDto cannot be null");
        }
        logger.info("Creating paper bundle: {}", dto.getName());
        PaperBundle entity = paperBundleMapper.toEntity(dto);

        // Manually set relationships
        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            entity.setSubject(subject);
        }
        if (dto.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
            entity.setLesson(lesson);
        }
        // Set createdBy (assume from context, placeholder)
        // entity.setCreatedBy(currentUser);

        PaperBundle saved = paperBundleRepository.save(entity);
        logger.info("Paper bundle created with ID: {}", saved.getId());
        return paperBundleMapper.toDto(saved);
    }

    /**
     * Retrieves all paper bundles.
     * 
     * @return a list of all PaperBundleDto
     */
    public List<PaperBundleDto> getAll() {
        logger.info("Fetching all paper bundles");
        List<PaperBundle> bundles = paperBundleRepository.findAll();
        List<PaperBundleDto> dtos = paperBundleMapper.toDtoList(bundles);
        logger.info("Found {} paper bundles", dtos.size());
        return dtos;
    }

    /**
     * Retrieves all paper bundle summaries (public view).
     * 
     * @return a list of PaperBundleSummaryDto
     */
    public List<PaperBundleSummaryDto> getAllSummaries() {
        logger.info("Fetching all paper bundle summaries");
        List<PaperBundle> bundles = paperBundleRepository.findAll();
        return paperBundleMapper.toSummaryDtoList(bundles);
    }

    /**
     * Retrieves paper bundle details for a user.
     * Checks if the user has purchased the bundle.
     * 
     * @param bundleId the ID of the bundle
     * @param userId   the ID of the user requesting details
     * @return PaperBundleDetailDto if access is granted
     * @throws SecurityException        if access is denied
     * @throws IllegalArgumentException if bundle not found
     */
    public PaperBundleDetailDto getBundleDetails(Long bundleId, Long userId) {
        logger.info("Fetching bundle details for bundle ID: {} and user ID: {}", bundleId, userId);
        PaperBundle bundle = paperBundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Paper bundle not found"));

        // Check access
        boolean hasAccess = studentBundleAccessRepository.existsByStudentIdAndBundleId(userId, bundleId);
        // Also allow if it's a free bundle or admin (logic can be expanded)
        // For now, strict check on purchase

        if (!hasAccess) {
            logger.warn("User {} denied access to bundle {}", userId, bundleId);
            throw new SecurityException("Access denied: Bundle not purchased");
        }

        return paperBundleMapper.toDetailDto(bundle);
    }

    /**
     * Purchases a bundle for a user.
     * Creates a StudentBundleAccess record.
     * 
     * @param bundleId the ID of the bundle
     * @param userId   the ID of the user purchasing
     */
    public void purchaseBundle(Long bundleId, Long userId) {
        logger.info("Purchasing bundle {} for user {}", bundleId, userId);

        // Check if bundle exists
        PaperBundle bundle = paperBundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Paper bundle not found"));

        // Check if already purchased
        if (studentBundleAccessRepository.existsByStudentIdAndBundleId(userId, bundleId)) {
            logger.info("User {} already has access to bundle {}", userId, bundleId);
            return;
        }

        // Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Create access record
        StudentBundleAccess access = new StudentBundleAccess();
        access.setStudent(user);
        access.setBundle(bundle);
        access.setPurchasedAt(LocalDateTime.now());

        studentBundleAccessRepository.save(access);
        logger.info("Bundle {} purchased successfully for user {}", bundleId, userId);
    }

    /**
     * Filter bundles by multiple criteria.
     * All parameters are optional (can be null).
     * 
     * @param type        the paper type filter
     * @param examType    the exam type filter
     * @param subjectId   the subject ID filter
     * @param lessonId    the lesson ID filter
     * @param isPastPaper the past paper status filter
     * @param minPrice    the minimum price filter
     * @param maxPrice    the maximum price filter
     * @param name        the name search term
     * @return a list of PaperBundleSummaryDto matching the criteria
     */
    public List<PaperBundleSummaryDto> filterBundles(
            PaperType type,
            String examType,
            Long subjectId,
            Long lessonId,
            Boolean isPastPaper,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String name) {
        logger.info(
                "Filtering bundles - type: {}, examType: {}, subjectId: {}, lessonId: {}, isPastPaper: {}, minPrice: {}, maxPrice: {}, name: {}",
                type, examType, subjectId, lessonId, isPastPaper, minPrice, maxPrice, name);

        String namePattern = null;
        if (name != null && !name.trim().isEmpty()) {
            namePattern = "%" + name.trim().toLowerCase() + "%";
        }

        List<PaperBundle> bundles = paperBundleRepository.findByFilters(
                type, examType, subjectId, lessonId, isPastPaper, minPrice, maxPrice, namePattern);

        List<PaperBundleSummaryDto> result = bundles.stream()
                .map(paperBundleMapper::toSummaryDto)
                .collect(Collectors.toList());

        logger.info("Found {} bundles matching filter criteria", result.size());
        return result;
    }

    /**
     * Search bundles by name (case-insensitive, partial match).
     * 
     * @param name the search term
     * @return a list of PaperBundleSummaryDto matching the name
     */
    public List<PaperBundleSummaryDto> searchByName(String name) {
        logger.info("Searching bundles by name: {}", name);

        List<PaperBundle> bundles = paperBundleRepository.findByNameContainingIgnoreCase(name);

        List<PaperBundleSummaryDto> result = bundles.stream()
                .map(paperBundleMapper::toSummaryDto)
                .collect(Collectors.toList());

        logger.info("Found {} bundles matching name '{}'", result.size(), name);
        return result;
    }
}
