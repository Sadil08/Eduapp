package com.eduapp.backend.service;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for admin bundle management operations.
 * Provides CRUD operations and statistics for paper bundles.
 */
@Service
public class AdminBundleService {

    private static final Logger logger = LoggerFactory.getLogger(AdminBundleService.class);

    private final PaperBundleRepository bundleRepository;
    private final PaperRepository paperRepository;
    private final StudentBundleAccessRepository accessRepository;
    private final StudentPaperAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public AdminBundleService(PaperBundleRepository bundleRepository,
            PaperRepository paperRepository,
            StudentBundleAccessRepository accessRepository,
            StudentPaperAttemptRepository attemptRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository) {
        this.bundleRepository = bundleRepository;
        this.paperRepository = paperRepository;
        this.accessRepository = accessRepository;
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get overall system statistics
     */
    public SystemStatsDto getSystemStats() {
        logger.info("Generating system statistics");

        int totalBundles = (int) bundleRepository.count();
        int totalPapers = (int) paperRepository.count();
        int totalQuestions = (int) questionRepository.count();
        int totalUsers = (int) userRepository.count();
        int totalAttempts = (int) attemptRepository.count();

        // Calculate total revenue from all bundle purchases
        // Calculate total revenue from all bundle purchases
        BigDecimal totalRevenue = accessRepository.sumPricePaid();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        SystemStatsDto stats = new SystemStatsDto(
                totalBundles, totalPapers, totalQuestions,
                totalUsers, totalAttempts, totalRevenue);

        logger.info("System stats generated: {} bundles, {} papers, {} users",
                totalBundles, totalPapers, totalUsers);

        return stats;
    }

    /**
     * Get statistics for a specific bundle
     */
    public BundleStatsDto getBundleStats(Long bundleId) {
        logger.info("Generating statistics for bundle ID: {}", bundleId);

        PaperBundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));

        int totalPapers = bundle.getPapers() != null ? bundle.getPapers().size() : 0;

        // Count total questions across all papers in bundle
        int totalQuestions = bundle.getPapers() != null ? bundle.getPapers().stream()
                .mapToInt(paper -> paper.getQuestions() != null ? paper.getQuestions().size() : 0)
                .sum() : 0;

        int totalStudentsWithAccess = accessRepository.countByBundleId(bundleId);

        // Count total attempts across all papers in bundle
        int totalAttempts = bundle.getPapers() != null ? bundle.getPapers().stream()
                .mapToInt(paper -> (int) attemptRepository.countByPaperId(paper.getId()))
                .sum() : 0;

        BundleStatsDto stats = new BundleStatsDto(
                bundleId, bundle.getName(), totalPapers, totalQuestions,
                totalStudentsWithAccess, totalAttempts);

        logger.info("Bundle stats for {}: {} papers, {} students, {} attempts",
                bundle.getName(), totalPapers, totalStudentsWithAccess, totalAttempts);

        return stats;
    }

    /**
     * Get all bundles with their statistics
     */
    public List<AdminBundleDto> getAllBundlesWithStats() {
        logger.info("Fetching all bundles with statistics");

        List<PaperBundle> bundles = bundleRepository.findAll();

        return bundles.stream().map(bundle -> {
            AdminBundleDto dto = new AdminBundleDto();
            // Copy basic fields from PaperBundle
            dto.setId(bundle.getId());
            dto.setName(bundle.getName());
            dto.setDescription(bundle.getDescription());
            dto.setPrice(bundle.getPrice());
            dto.setType(bundle.getType());
            dto.setExamType(bundle.getExamType());
            dto.setSubjectId(bundle.getSubject() != null ? bundle.getSubject().getId() : null);
            dto.setLessonId(bundle.getLesson() != null ? bundle.getLesson().getId() : null);
            dto.setIsPastPaper(bundle.getIsPastPaper());

            // Set admin-specific fields
            dto.setCreatedAt(bundle.getCreatedAt());
            dto.setUpdatedAt(bundle.getUpdatedAt());
            dto.setCreatedBy(bundle.getCreatedBy() != null ? bundle.getCreatedBy().getId() : null);

            // Add statistics
            dto.setStats(getBundleStats(bundle.getId()));

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Create a new bundle
     */
    @Transactional
    public PaperBundle createBundle(PaperBundleDto dto, Long adminId) {
        logger.info("Creating new bundle: {} by admin ID: {}", dto.getName(), adminId);

        PaperBundle bundle = new PaperBundle();
        bundle.setName(dto.getName());
        bundle.setDescription(dto.getDescription());
        bundle.setPrice(dto.getPrice());
        bundle.setType(dto.getType());
        bundle.setExamType(dto.getExamType());
        bundle.setIsPastPaper(dto.getIsPastPaper());
        // Set createdBy only if adminId is provided
        if (adminId != null) {
            userRepository.findById(adminId).ifPresent(bundle::setCreatedBy);
        }

        PaperBundle saved = bundleRepository.save(bundle);
        logger.info("Bundle created with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Update an existing bundle
     */
    @Transactional
    public PaperBundle updateBundle(Long id, PaperBundleDto dto) {
        logger.info("Updating bundle ID: {}", id);

        PaperBundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));

        bundle.setName(dto.getName());
        bundle.setDescription(dto.getDescription());
        bundle.setPrice(dto.getPrice());
        bundle.setType(dto.getType());
        bundle.setExamType(dto.getExamType());
        bundle.setIsPastPaper(dto.getIsPastPaper());

        PaperBundle updated = bundleRepository.save(bundle);
        logger.info("Bundle updated: {}", updated.getName());

        return updated;
    }

    /**
     * Delete a bundle
     */
    @Transactional
    public void deleteBundle(Long id) {
        logger.info("Deleting bundle ID: {}", id);

        if (!bundleRepository.existsById(id)) {
            throw new IllegalArgumentException("Bundle not found");
        }

        bundleRepository.deleteById(id);
        logger.info("Bundle deleted: {}", id);
    }

    /**
     * Add a paper to a bundle
     */
    @Transactional
    public void addPaperToBundle(Long bundleId, Long paperId) {
        logger.info("Adding paper ID: {} to bundle ID: {}", paperId, bundleId);

        PaperBundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        paper.setBundle(bundle);
        paperRepository.save(paper);

        logger.info("Paper {} added to bundle {}", paper.getName(), bundle.getName());
    }

    /**
     * Remove a paper from a bundle
     */
    @Transactional
    public void removePaperFromBundle(Long bundleId, Long paperId) {
        logger.info("Removing paper ID: {} from bundle ID: {}", paperId, bundleId);

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        if (paper.getBundle() == null || !paper.getBundle().getId().equals(bundleId)) {
            throw new IllegalArgumentException("Paper is not in this bundle");
        }

        paper.setBundle(null);
        paperRepository.save(paper);

        logger.info("Paper {} removed from bundle", paper.getName());
    }
}
