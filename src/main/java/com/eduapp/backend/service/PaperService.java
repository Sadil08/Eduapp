package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaperAttemptDto;
import com.eduapp.backend.dto.PaperDto;
import com.eduapp.backend.dto.PaperSubmissionDto;
import com.eduapp.backend.dto.StudentPaperAttemptDto;
import com.eduapp.backend.mapper.PaperMapper;
import com.eduapp.backend.mapper.StudentPaperAttemptMapper;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.PaperBundleRepository;
import com.eduapp.backend.repository.StudentBundleAccessRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import com.eduapp.backend.repository.StudentAnswerRepository;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.repository.QuestionRepository;
import com.eduapp.backend.repository.QuestionOptionRepository;
import com.eduapp.backend.repository.OverallPaperAnalysisRepository;
import com.eduapp.backend.repository.ExtraAttemptPurchaseRepository;
import com.eduapp.backend.repository.CustomBundleRepository;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.model.User;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.model.CustomBundle;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Paper entities.
 * Provides business logic for CRUD operations on papers, including validation
 * of associated bundles.
 * Follows Single Responsibility Principle by handling only paper-related
 * operations.
 */
@Service
@SuppressWarnings("null")
public class PaperService {

    private static final Logger logger = LoggerFactory.getLogger(PaperService.class);

    private final PaperRepository paperRepository;
    private final PaperBundleRepository paperBundleRepository;
    private final CustomBundleRepository customBundleRepository;
    private final StudentBundleAccessRepository studentBundleAccessRepository;
    private final StudentPaperAttemptRepository studentPaperAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final PaperMapper paperMapper;
    private final StudentPaperAttemptMapper studentPaperAttemptMapper;
    private final OverallPaperAnalysisRepository overallPaperAnalysisRepository;
    private final ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository;
    private final AIAnalysisService aiAnalysisService;
    private final com.eduapp.backend.service.ExtractionTrackingService extractionTrackingService;
    private final StudentAnswerService studentAnswerService;

    /**
     * Constructor for dependency injection of repositories.
     * 
     * @param paperRepository               the repository for Paper entities
     * @param paperBundleRepository         the repository for PaperBundle entities
     * @param customBundleRepository        the repository for CustomBundle entities
     * @param studentBundleAccessRepository the repository for StudentBundleAccess
     *                                      entities
     * @param paperMapper                   the mapper for Paper DTOs
     */
    public PaperService(PaperRepository paperRepository,
            PaperBundleRepository paperBundleRepository,
            CustomBundleRepository customBundleRepository,
            StudentBundleAccessRepository studentBundleAccessRepository,
            StudentPaperAttemptRepository studentPaperAttemptRepository,
            StudentAnswerRepository studentAnswerRepository,
            UserRepository userRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            PaperMapper paperMapper,
            StudentPaperAttemptMapper studentPaperAttemptMapper,
            OverallPaperAnalysisRepository overallPaperAnalysisRepository,
            ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository,

            AIAnalysisService aiAnalysisService,
            com.eduapp.backend.service.ExtractionTrackingService extractionTrackingService,
            StudentAnswerService studentAnswerService) {
        this.paperRepository = paperRepository;
        this.paperBundleRepository = paperBundleRepository;
        this.customBundleRepository = customBundleRepository;
        this.studentBundleAccessRepository = studentBundleAccessRepository;
        this.studentPaperAttemptRepository = studentPaperAttemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.paperMapper = paperMapper;
        this.studentPaperAttemptMapper = studentPaperAttemptMapper;
        this.overallPaperAnalysisRepository = overallPaperAnalysisRepository;
        this.extraAttemptPurchaseRepository = extraAttemptPurchaseRepository;
        this.aiAnalysisService = aiAnalysisService;
        this.extractionTrackingService = extractionTrackingService;
        this.studentAnswerService = studentAnswerService;
    }

    /**
     * Retrieves all papers from the database.
     * 
     * @return a list of all Paper entities
     */
    public List<Paper> findAll() {
        logger.info("Fetching all papers");
        List<Paper> papers = paperRepository.findAll();
        logger.info("Found {} papers", papers.size());
        return papers;
    }

    /**
     * Retrieves a paper by its ID.
     * 
     * @param id the ID of the paper to retrieve
     * @return an Optional containing the Paper if found, or empty if not
     */
    public Optional<Paper> findById(Long id) {
        logger.info("Fetching paper with ID: {}", id);
        Optional<Paper> paper = paperRepository.findById(id);
        if (paper.isPresent()) {
            logger.info("Paper found: {}", paper.get().getName());
        } else {
            logger.warn("Paper with ID {} not found", id);
        }
        return paper;
    }

    /**
     * Saves a new or updated paper to the database.
     * Validates that the associated bundle exists.
     * 
     * @param paper the Paper entity to save
     * @return the saved Paper entity
     * @throws IllegalArgumentException if the bundle does not exist
     */
    public Paper save(Paper paper) {
        logger.info("Saving paper: {}", paper.getName());
        // Validate all assigned bundles exist
        if (paper.getBundles() != null && !paper.getBundles().isEmpty()) {
            for (PaperBundle bundle : paper.getBundles()) {
                if (bundle.getId() != null && !paperBundleRepository.existsById(bundle.getId())) {
                    logger.error("PaperBundle with ID {} does not exist", bundle.getId());
                    throw new IllegalArgumentException("PaperBundle does not exist: " + bundle.getId());
                }
            }
        }
        Paper savedPaper = paperRepository.save(paper);
        logger.info("Paper saved with ID: {}", savedPaper.getId());
        return savedPaper;
    }

    /**
     * Updates an existing paper with data from DTO.
     * Handles bundle assignment logic.
     * 
     * @param id the ID of the paper to update
     * @param dto the DTO containing updated data
     * @return the updated Paper entity
     * @throws IllegalArgumentException if paper not found
     */
    @Transactional
    public Paper updatePaper(Long id, PaperDto dto) {
        logger.info("Updating paper with ID: {}", id);
        
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        paper.setName(dto.getName());
        paper.setDescription(dto.getDescription());
        paper.setType(dto.getType());
        paper.setMaxFreeAttempts(dto.getMaxFreeAttempts());
        if (dto.getTotalMarks() != null) {
            paper.setTotalMarks(dto.getTotalMarks());
        }
        
        // Update Bundles
        if (dto.getBundleIds() != null) {
            List<PaperBundle> bundles = paperBundleRepository.findAllById(dto.getBundleIds());
            paper.setBundles(bundles);
            logger.info("Updated bundles for paper {}: count={}", id, bundles.size());
        }

        return paperRepository.save(paper);
    }

    /**
     * Deletes a paper by its ID.
     * 
     * @param id the ID of the paper to delete
     */
    public void deleteById(Long id) {
        logger.info("Deleting paper with ID: {}", id);
        if (paperRepository.existsById(id)) {
            paperRepository.deleteById(id);
            logger.info("Paper deleted successfully");
        } else {
            logger.warn("Paper with ID {} not found for deletion", id);
        }
    }

    /**
     * Checks if a paper exists by its ID.
     * 
     * @param id the ID to check
     * @return true if the paper exists, false otherwise
     */
    public boolean existsById(Long id) {
        boolean exists = paperRepository.existsById(id);
        logger.debug("Paper existence check for ID {}: {}", id, exists);
        return exists;
    }

    /**
     * Checks if a user can attempt a paper based on attempt limits.
     * Calculates total allowed attempts (free + purchased) and compares with
     * attempts made.
     * 
     * @param paperId the ID of the paper
     * @param userId  the ID of the user
     * @return AttemptLimitInfo containing attempt counts and whether user can
     *         attempt
     */
    private AttemptLimitInfo checkAttemptLimit(Long paperId, Long userId) {
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        int attemptsMade = studentPaperAttemptRepository.countByStudentIdAndPaperId(userId, paperId);
        int freeAttempts = paper.getMaxFreeAttempts() != null ? paper.getMaxFreeAttempts() : 2;
        int extraAttempts = extraAttemptPurchaseRepository.sumExtraAttemptsByUserAndPaper(userId, paperId);
        int maxAttempts = freeAttempts + extraAttempts;
        int remainingAttempts = Math.max(0, maxAttempts - attemptsMade);
        boolean canAttempt = attemptsMade < maxAttempts;

        logger.info("Attempt limit check for user {} on paper {}: made={}, max={}, remaining={}, canAttempt={}",
                userId, paperId, attemptsMade, maxAttempts, remainingAttempts, canAttempt);

        return new AttemptLimitInfo(attemptsMade, maxAttempts, remainingAttempts, canAttempt);
    }
    
    /**
     * Checks attempt limit for a specific paper, user, and bundle context.
     * This method is used for many-to-many paper-bundle relationships where
     * attempts are scoped by the bundle they were made from.
     *
     * @param paperId  the ID of the paper
     * @param userId   the ID of the user
     * @param bundleId the ID of the bundle context
     * @return AttemptLimitInfo containing attempt counts scoped by bundle
     */
    private AttemptLimitInfo checkAttemptLimit(Long paperId, Long userId, Long bundleId) {
        return checkAttemptLimit(paperId, userId, bundleId, null);
    }

    private AttemptLimitInfo checkAttemptLimit(Long paperId, Long userId, Long bundleId, Long customBundleId) {
        if (customBundleId != null) {
             // Custom Bundle Logic
             CustomBundle cb = customBundleRepository.findById(customBundleId)
                     .orElseThrow(() -> new IllegalArgumentException("Custom Bundle not found"));
             
             logger.info("Checking custom bundle {} papers count: {}", customBundleId, cb.getPapers().size());
             if (cb.getPapers().stream().noneMatch(p -> p.getId().equals(paperId))) {
                 logger.error("Paper {} not found in custom bundle {}. Bundle papers: {}", paperId, customBundleId, cb.getPapers().stream().map(Paper::getId).toList());
                 throw new IllegalArgumentException("Paper not in specified custom bundle");
             }

             // Access Check: Currently only Creator is supported fully as per current implementation context
             if (!cb.getCreator().getId().equals(userId)) {
                 throw new SecurityException("Access denied to custom bundle");
             }
             
             int attemptsMade = studentPaperAttemptRepository.countByStudentIdAndPaperIdAndOriginCustomBundleId(userId, paperId, customBundleId);
             int maxAttempts = 2; // Fixed limit for custom bundles
             
             // Check if there's an active IN_PROGRESS attempt that can be resumed
             boolean hasActiveAttempt = !studentPaperAttemptRepository
                 .findByStudentIdAndPaperIdAndOriginCustomBundleIdAndStatusOrderByStartedAtDesc(
                     userId, paperId, customBundleId, com.eduapp.backend.model.AttemptStatus.IN_PROGRESS)
                 .isEmpty();
             
             // canAttempt is true if: can start new attempt OR can resume existing one
             boolean canAttempt = attemptsMade < maxAttempts || hasActiveAttempt;
             
             logger.info("Custom bundle attempt check: user={}, paper={}, customBundle={}, made={}, max={}, hasActive={}, canAttempt={}",
                 userId, paperId, customBundleId, attemptsMade, maxAttempts, hasActiveAttempt, canAttempt);
             
             return new AttemptLimitInfo(attemptsMade, maxAttempts, Math.max(0, maxAttempts - attemptsMade), canAttempt);
        }

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));
                
        // Count attempts scoped by bundle
        int attemptsMade = studentPaperAttemptRepository.countByStudentIdAndPaperIdAndOriginBundleId(userId, paperId, bundleId);
        int freeAttempts = paper.getMaxFreeAttempts() != null ? paper.getMaxFreeAttempts() : 2;
        // Extra attempts are also bundle-scoped now
        int extraAttempts = extraAttemptPurchaseRepository.sumExtraAttemptsByUserAndPaperAndBundle(userId, paperId, bundleId);
        int maxAttempts = freeAttempts + extraAttempts;
        int remainingAttempts = Math.max(0, maxAttempts - attemptsMade);
        
        // Check if there's an active IN_PROGRESS attempt that can be resumed
        boolean hasActiveAttempt = !studentPaperAttemptRepository
            .findByStudentIdAndPaperIdAndOriginBundleIdAndStatusOrderByStartedAtDesc(
                userId, paperId, bundleId, com.eduapp.backend.model.AttemptStatus.IN_PROGRESS)
            .isEmpty();
        
        // canAttempt is true if: can start new attempt OR can resume existing one
        boolean canAttempt = attemptsMade < maxAttempts || hasActiveAttempt;

        logger.info("Attempt limit check for user {} on paper {} bundle {}: made={}, max={} (free={}, extra={}), remaining={}, hasActive={}, canAttempt={}",
                userId, paperId, bundleId, attemptsMade, maxAttempts, freeAttempts, extraAttempts, remainingAttempts, hasActiveAttempt, canAttempt);

        return new AttemptLimitInfo(attemptsMade, maxAttempts, remainingAttempts, canAttempt);
    }

    /**
     * Inner class to hold attempt limit information
     */
    private static class AttemptLimitInfo {
        final int attemptsMade;
        final int maxAttempts;
        final int remainingAttempts;
        final boolean canAttempt;

        AttemptLimitInfo(int attemptsMade, int maxAttempts, int remainingAttempts, boolean canAttempt) {
            this.attemptsMade = attemptsMade;
            this.maxAttempts = maxAttempts;
            this.remainingAttempts = remainingAttempts;
            this.canAttempt = canAttempt;
        }
    }

    /**
     * Retrieves paper details for an attempt.
     * Checks if the user has purchased the parent bundle.
     * 
     * @param paperId the ID of the paper
     * @param userId  the ID of the user requesting the attempt
     * @return PaperDetailDto if access is granted
     * @throws SecurityException        if access is denied
     * @throws IllegalArgumentException if paper not found
     */
    /**
     * Get paper attempt for student.
     * @param paperId Paper ID
     * @param userId User ID
     * @param bundleId The bundle context for this attempt (required for many-to-many)
     * @param forceNew If true, abandon existing IN_PROGRESS attempt and start fresh
     * @return PaperAttemptDto
     */
    public PaperAttemptDto getPaperAttempt(Long paperId, Long userId, Long bundleId, boolean forceNew) {
        return getPaperAttempt(paperId, userId, bundleId, null, forceNew);
    }

    @Transactional
    public PaperAttemptDto getPaperAttempt(Long paperId, Long userId, Long bundleId, Long customBundleId, boolean forceNew) {
        logger.info("Getting paper attempt for paper {} user {} bundle {} customBundle {} (forceNew={})", 
                paperId, userId, bundleId, customBundleId, forceNew);

        // Check attempt limits (scoped by bundle or custom bundle)
        AttemptLimitInfo limitInfo = checkAttemptLimit(paperId, userId, bundleId, customBundleId);

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        PaperAttemptDto dto = paperMapper.toAttemptDto(paper);
        dto.setAttemptsMade(limitInfo.attemptsMade);
        dto.setMaxAttempts(limitInfo.maxAttempts);
        dto.setRemainingAttempts(limitInfo.remainingAttempts);
        dto.setCanAttempt(limitInfo.canAttempt);
        dto.setOriginBundleId(bundleId);
        dto.setOriginCustomBundleId(customBundleId);

        // Find existing active attempt or create a new one if allowed (scoped by bundle)
        List<StudentPaperAttempt> activeAttempts;
        if (customBundleId != null) {
            activeAttempts = studentPaperAttemptRepository
                .findByStudentIdAndPaperIdAndOriginCustomBundleIdAndStatusOrderByStartedAtDesc(userId, paperId, customBundleId, com.eduapp.backend.model.AttemptStatus.IN_PROGRESS);
        } else {
            activeAttempts = studentPaperAttemptRepository
                .findByStudentIdAndPaperIdAndOriginBundleIdAndStatusOrderByStartedAtDesc(userId, paperId, bundleId, com.eduapp.backend.model.AttemptStatus.IN_PROGRESS);
        }

        if (!activeAttempts.isEmpty()) {
            StudentPaperAttempt latestActive = activeAttempts.get(0);
            
            // If forceNew is requested, abandon existing attempt and create new one
            if (forceNew) {
                logger.info("Force new attempt requested - abandoning attempt {} for user {} paper {} bundle {} customBundle {}", 
                    latestActive.getId(), userId, paperId, bundleId, customBundleId);
                latestActive.setStatus(com.eduapp.backend.model.AttemptStatus.ABANDONED);
                latestActive.setCompletedAt(LocalDateTime.now());
                studentPaperAttemptRepository.save(latestActive);
                
                // Create fresh attempt
                if (limitInfo.canAttempt) {
                    createAndSaveAttempt(userId, paper, bundleId, customBundleId, dto, limitInfo);
                } else {
                    logger.warn("Cannot create new attempt - limit reached for user {} paper {} bundle {} customBundle {}", userId, paperId, bundleId, customBundleId);
                }
            } else {
                dto.setAttemptId(latestActive.getId());
                logger.info("Found active attempt {} for user {} paper {} bundle {} customBundle {}", latestActive.getId(), userId, paperId, bundleId, customBundleId);
                
                if (activeAttempts.size() > 1) {
                    logger.warn("Found {} active attempts for user {} paper {} bundle {} customBundle {}. Using latest: {}", 
                        activeAttempts.size(), userId, paperId, bundleId, customBundleId, latestActive.getId());
                }
            }
        } else if (limitInfo.canAttempt) {
            createAndSaveAttempt(userId, paper, bundleId, customBundleId, dto, limitInfo);
        } else {
            logger.warn("No active attempt and limit reached for user {} paper {} bundle {} customBundle {}", userId, paperId, bundleId, customBundleId);
        }

        // Populate extraction counts for each question if attemptId is available
        if (dto.getAttemptId() != null) {
            java.util.Map<Long, Integer> extractionCounts = 
                extractionTrackingService.getAllExtractionCounts(dto.getAttemptId());
            
            logger.info("Populating extraction counts for attempt {}: found {} tracked questions", 
                dto.getAttemptId(), extractionCounts.size());
            
            for (com.eduapp.backend.dto.QuestionAttemptDto questionDto : dto.getQuestions()) {
                Integer extractionsUsed = extractionCounts.getOrDefault(questionDto.getId(), 0);
                questionDto.setExtractionsUsed(extractionsUsed);
            }
        } else {
            logger.warn("No attemptId available, cannot populate extraction counts!");
        }

        return dto;
    }

    private void createAndSaveAttempt(Long userId, Paper paper, Long bundleId, Long customBundleId, PaperAttemptDto dto, AttemptLimitInfo limitInfo) {
        StudentPaperAttempt newAttempt = new StudentPaperAttempt();
        newAttempt.setStudent(userRepository.getReferenceById(userId));
        newAttempt.setPaper(paper);
        if (customBundleId != null) {
            newAttempt.setOriginCustomBundle(customBundleRepository.getReferenceById(customBundleId));
        } else {
            newAttempt.setOriginBundle(paperBundleRepository.getReferenceById(bundleId));
        }
        newAttempt.setStartedAt(LocalDateTime.now());
        newAttempt.setStatus(com.eduapp.backend.model.AttemptStatus.IN_PROGRESS);
        newAttempt.setAttemptNumber(limitInfo.attemptsMade + 1);
        
        StudentPaperAttempt saved = studentPaperAttemptRepository.save(newAttempt);
        dto.setAttemptId(saved.getId());
        logger.info("Created new attempt {} for user {} paper {} bundle {} customBundle {} (Thread: {})", 
            saved.getId(), userId, paper.getId(), bundleId, customBundleId, Thread.currentThread().getName());
        
        // Initialize extraction tracking
        extractionTrackingService.initializeTrackingForAttempt(saved);
        logger.info("Initialized extraction tracking for attempt {}", saved.getId());
    }

    @Transactional
    public StudentPaperAttempt submitPaperAttempt(Long paperId, Long userId, Long bundleId, PaperSubmissionDto submission) {
        return submitPaperAttempt(paperId, userId, bundleId, null, submission);
    }

    @Transactional
    public StudentPaperAttempt submitPaperAttempt(Long paperId, Long userId, Long bundleId, Long customBundleId, PaperSubmissionDto submission) {
        logger.info("Submitting paper attempt for paper ID: {} user ID: {} bundle ID: {} customBundle ID: {}", 
                paperId, userId, bundleId, customBundleId);

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        User student = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate bundle ctx
        if (customBundleId != null) {
             // Custom Bundle Logic
             CustomBundle cb = customBundleRepository.findById(customBundleId)
                     .orElseThrow(() -> new IllegalArgumentException("Custom Bundle not found"));
             
             if (cb.getPapers().stream().noneMatch(p -> p.getId().equals(paperId))) {
                 throw new IllegalArgumentException("Paper not in specified custom bundle");
             }
             if (!cb.getCreator().getId().equals(userId)) {
                 throw new SecurityException("Access denied to custom bundle");
             }
        } else {
            // Standard Bundle Logic
            PaperBundle bundle = paperBundleRepository.findById(bundleId)
                    .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));
            boolean paperBelongsToBundle = paper.getBundles().stream()
                    .anyMatch(b -> b.getId().equals(bundleId));
            if (!paperBelongsToBundle) {
                throw new IllegalArgumentException("Paper does not belong to the specified bundle");
            }
            if (!studentBundleAccessRepository.existsByStudentIdAndBundleId(userId, bundleId)) {
                throw new SecurityException("Access denied: Bundle not purchased");
            }
        }

        // Find existing IN_PROGRESS attempt to update
        StudentPaperAttempt attempt;
        if (customBundleId != null) {
            attempt = studentPaperAttemptRepository
                .findByStudentIdAndPaperIdAndOriginCustomBundleIdAndStatusOrderByStartedAtDesc(
                    userId, paperId, customBundleId, com.eduapp.backend.model.AttemptStatus.IN_PROGRESS)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No active attempt found to submit"));
        } else {
             attempt = studentPaperAttemptRepository
                .findByStudentIdAndPaperIdAndOriginBundleIdAndStatusOrderByStartedAtDesc(
                    userId, paperId, bundleId, com.eduapp.backend.model.AttemptStatus.IN_PROGRESS)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No active attempt found to submit"));
        }

        // Update attempt details
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setTimeTakenMinutes(submission.getTimeTakenMinutes());
        attempt.setStatus(com.eduapp.backend.model.AttemptStatus.SUBMITTED);
        
        // Ensure startedAt is consistent if it was set (it should exist from creation)
        if (attempt.getStartedAt() == null) {
             attempt.setStartedAt(LocalDateTime.now().minusMinutes(
                 submission.getTimeTakenMinutes() != null ? submission.getTimeTakenMinutes() : 0));
        }

        StudentPaperAttempt savedAttempt = studentPaperAttemptRepository.save(attempt);

        // Create a map of submitted answers for quick lookup
        java.util.Map<Long, PaperSubmissionDto.StudentAnswerSubmissionDto> submittedAnswersMap = new java.util.HashMap<>();
        if (submission.getAnswers() != null) {
            for (PaperSubmissionDto.StudentAnswerSubmissionDto ansDto : submission.getAnswers()) {
                submittedAnswersMap.put(ansDto.getQuestionId(), ansDto);
            }
        }

        // IMPORTANT: Query ALL existing answers for this attempt ONCE before the loop
        // This avoids Hibernate auto-flush issues when querying inside the loop
        List<StudentAnswer> existingAnswers = studentAnswerRepository.findByAttemptId(savedAttempt.getId());
        java.util.Map<Long, StudentAnswer> existingAnswersMap = new java.util.HashMap<>();
        for (StudentAnswer ea : existingAnswers) {
            existingAnswersMap.put(ea.getQuestion().getId(), ea);
        }
        logger.info("Found {} existing answers for attempt {}", existingAnswers.size(), savedAttempt.getId());

        // Create or update StudentAnswer for ALL questions in the paper (including unanswered ones)
        for (Question question : paper.getQuestions()) {
            // Check if answer already exists for this question
            StudentAnswer answer = existingAnswersMap.get(question.getId());
            boolean isNew = (answer == null);
            
            if (isNew) {
                answer = new StudentAnswer();
                answer.setAttempt(savedAttempt);
                answer.setQuestion(question);
            }

            PaperSubmissionDto.StudentAnswerSubmissionDto ansDto = submittedAnswersMap.get(question.getId());

            if (ansDto != null) {
                // Question was answered
                if (ansDto.getSelectedOptionId() != null) {
                    // MCQ
                    QuestionOption option = questionOptionRepository.findById(ansDto.getSelectedOptionId())
                            .orElseThrow(() -> new IllegalArgumentException("Option not found: " + ansDto.getSelectedOptionId()));
                    answer.setSelectedOption(option);
                    answer.setAnswerText(option.getText());
                } else {
                    // Text/Essay
                    answer.setAnswerText(ansDto.getAnswerText());
                    answer.setImageUrl(ansDto.getImageUrl());
                    answer.setExtractedText(ansDto.getExtractedText());
                }
            } else {
                // Not answered
                answer.setAnswerText(null);
                answer.setMarksAwarded(0); 
            }

            // MARK AS FINAL SUBMISSION (NOT DRAFT)
            answer.setIsDraft(false);

            // Save the answer (either new or updated existing)
            StudentAnswer savedAnswer = studentAnswerRepository.save(answer);
            if (isNew) {
                savedAttempt.getAnswers().add(savedAnswer);
            }
        }
        


        logger.info("Loaded attempt with {} answers for AI analysis", savedAttempt.getAnswers().size());

        // Trigger AI Analysis asynchronously
        try {
            aiAnalysisService.analyzeAttempt(savedAttempt);
        } catch (Exception e) {
            logger.error("Failed to trigger AI analysis for attempt: {}", savedAttempt.getId(), e);
        }

        return savedAttempt;
    }

    /**
     * Retrieves a student's paper attempt with AI analysis results.
     * Includes answers with marks/feedback and overall analysis.
     * 
     * @param attemptId the ID of the attempt to retrieve
     * @param userId    the ID of the user requesting the attempt
     * @return StudentPaperAttemptDto with complete results
     * @throws SecurityException        if attempt doesn't belong to user
     * @throws IllegalArgumentException if attempt not found
     */
    public StudentPaperAttemptDto getAttemptResults(Long attemptId, Long userId) {
        logger.info("Fetching attempt results for attempt ID: {} and user ID: {}", attemptId, userId);

        // Fetch attempt with security check
        StudentPaperAttempt attempt = studentPaperAttemptRepository.findByIdAndStudentId(attemptId, userId)
                .orElseThrow(() -> {
                    logger.warn("Attempt {} not found for user {}", attemptId, userId);
                    return new SecurityException("Attempt not found or access denied");
                });

        // Map to DTO
        StudentPaperAttemptDto dto = studentPaperAttemptMapper.toDto(attempt);

        // Debug logging for image display
        if (dto.getAnswers() != null) {
            dto.getAnswers().forEach(ans -> {
                logger.info("Retrieved answer for question {}: questionImg='{}', studentImg='{}', hideQ='{}'",
                        ans.getQuestionId(), ans.getQuestionImageUrl(), ans.getImageUrl(), ans.getHideQuestionText());
            });
        }

        // Fetch and add overall analysis if available
        overallPaperAnalysisRepository.findByAttemptId(attemptId).ifPresent(analysis -> {
            dto.setOverallFeedback(analysis.getOverallFeedback());
            dto.setTotalMarks(analysis.getTotalMarks());
        });

        logger.info("Successfully retrieved attempt results for attempt ID: {}", attemptId);
        return dto;
    }

    /**
     * Get attempt information for multiple papers for a specific user.
     * Returns a map of paper ID to attempt info.
     * 
     * @param paperIds List of paper IDs to get info for
     * @param userId   ID of the user
     * @return Map of paper ID to PaperAttemptInfoDto
     * @param bundleId Optional bundle context for bundle-scoped counting
     */
    public java.util.Map<Long, com.eduapp.backend.dto.PaperAttemptInfoDto> getAttemptInfoForPapers(
            List<Long> paperIds, Long userId, Long bundleId) {
        return getAttemptInfoForPapers(paperIds, userId, bundleId, null);
    }

    @Transactional(readOnly = true)
    public java.util.Map<Long, com.eduapp.backend.dto.PaperAttemptInfoDto> getAttemptInfoForPapers(
            List<Long> paperIds, Long userId, Long bundleId, Long customBundleId) {
        logger.info("Getting attempt info for {} papers for user {} (bundleId={}, customBundleId={})", 
                paperIds.size(), userId, bundleId, customBundleId);

        java.util.Map<Long, com.eduapp.backend.dto.PaperAttemptInfoDto> result = new java.util.HashMap<>();

        for (Long paperId : paperIds) {
            try {
                AttemptLimitInfo info = checkAttemptLimit(paperId, userId, bundleId, customBundleId);
                
                // Find in-progress attempt ID for Resume functionality
                Long inProgressAttemptId = null;
                List<StudentPaperAttempt> activeAttempts;
                if (customBundleId != null) {
                    activeAttempts = studentPaperAttemptRepository
                        .findByStudentIdAndPaperIdAndOriginCustomBundleIdAndStatusOrderByStartedAtDesc(
                            userId, paperId, customBundleId, com.eduapp.backend.model.AttemptStatus.IN_PROGRESS);
                } else {
                    activeAttempts = studentPaperAttemptRepository
                        .findByStudentIdAndPaperIdAndOriginBundleIdAndStatusOrderByStartedAtDesc(
                            userId, paperId, bundleId, com.eduapp.backend.model.AttemptStatus.IN_PROGRESS);
                }
                
                if (!activeAttempts.isEmpty()) {
                    inProgressAttemptId = activeAttempts.get(0).getId();
                }

                com.eduapp.backend.dto.PaperAttemptInfoDto dto = new com.eduapp.backend.dto.PaperAttemptInfoDto(
                        paperId, info.attemptsMade, info.maxAttempts, info.remainingAttempts, info.canAttempt, inProgressAttemptId);

                result.put(paperId, dto);
            } catch (Exception e) {
                logger.error("Error getting attempt info for paper {}: {}", paperId, e.getMessage());
            }
        }

        return result;
    }
}