package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaperAttemptDto;
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
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.model.User;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.model.OverallPaperAnalysis;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    /**
     * Constructor for dependency injection of repositories.
     * 
     * @param paperRepository               the repository for Paper entities
     * @param paperBundleRepository         the repository for PaperBundle entities
     * @param studentBundleAccessRepository the repository for StudentBundleAccess
     *                                      entities
     * @param paperMapper                   the mapper for Paper DTOs
     */
    public PaperService(PaperRepository paperRepository,
            PaperBundleRepository paperBundleRepository,
            StudentBundleAccessRepository studentBundleAccessRepository,
            StudentPaperAttemptRepository studentPaperAttemptRepository,
            StudentAnswerRepository studentAnswerRepository,
            UserRepository userRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            PaperMapper paperMapper,
            StudentPaperAttemptMapper studentPaperAttemptMapper,
            OverallPaperAnalysisRepository overallPaperAnalysisRepository,
            ExtraAttemptPurchaseRepository extraAttemptPurchaseRepository) {
        this.paperRepository = paperRepository;
        this.paperBundleRepository = paperBundleRepository;
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
        if (paper.getBundle() != null && paper.getBundle().getId() != null) {
            Optional<PaperBundle> bundle = paperBundleRepository.findById(paper.getBundle().getId());
            if (bundle.isEmpty()) {
                logger.error("PaperBundle with ID {} does not exist", paper.getBundle().getId());
                throw new IllegalArgumentException("PaperBundle does not exist");
            }
        }
        Paper savedPaper = paperRepository.save(paper);
        logger.info("Paper saved with ID: {}", savedPaper.getId());
        return savedPaper;
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
     * Retrieves paper details for an attempt.
     * Checks if the user has purchased the parent bundle.
     * 
     * @param paperId the ID of the paper
     * @param userId  the ID of the user requesting the attempt
     * @return PaperAttemptDto if access is granted
     * @throws SecurityException        if access is denied
     * @throws IllegalArgumentException if paper not found
     */
    public PaperAttemptDto getPaperAttempt(Long paperId, Long userId) {
        logger.info("Fetching paper attempt for paper ID: {} and user ID: {}", paperId, userId);
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        PaperBundle bundle = paper.getBundle();
        if (bundle == null) {
            throw new IllegalStateException("Paper is not associated with any bundle");
        }

        // Check access
        boolean hasAccess = studentBundleAccessRepository.existsByStudentIdAndBundleId(userId, bundle.getId());

        if (!hasAccess) {
            logger.warn("User {} denied access to paper {} (Bundle {})", userId, paperId, bundle.getId());
            throw new SecurityException("Access denied: Bundle not purchased");
        }

        // Check attempt limits
        AttemptLimitInfo limitInfo = checkAttemptLimit(paperId, userId);

        PaperAttemptDto dto = paperMapper.toAttemptDto(paper);
        dto.setAttemptsMade(limitInfo.attemptsMade);
        dto.setMaxAttempts(limitInfo.maxAttempts);
        dto.setRemainingAttempts(limitInfo.remainingAttempts);
        dto.setCanAttempt(limitInfo.canAttempt);

        return dto;
    }

    public StudentPaperAttempt submitPaperAttempt(Long paperId, Long userId, PaperSubmissionDto submission) {
        logger.info("Submitting paper attempt for paper ID: {} and user ID: {}", paperId, userId);

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        User student = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify access again just in case
        PaperBundle bundle = paper.getBundle();
        if (!studentBundleAccessRepository.existsByStudentIdAndBundleId(userId, bundle.getId())) {
            throw new SecurityException("Access denied: Bundle not purchased");
        }

        // Check attempt limits BEFORE creating the attempt
        AttemptLimitInfo limitInfo = checkAttemptLimit(paperId, userId);
        if (!limitInfo.canAttempt) {
            logger.warn("User {} exceeded attempt limit for paper {}. Attempts made: {}, Max allowed: {}",
                    userId, paperId, limitInfo.attemptsMade, limitInfo.maxAttempts);
            throw new SecurityException("Attempt limit exceeded. You have used all " + limitInfo.maxAttempts
                    + " attempts. Purchase extra attempts to continue.");
        }

        // Create Attempt
        StudentPaperAttempt attempt = new StudentPaperAttempt();
        attempt.setStudent(student);
        attempt.setPaper(paper);
        attempt.setStartedAt(LocalDateTime.now()
                .minusMinutes(submission.getTimeTakenMinutes() != null ? submission.getTimeTakenMinutes() : 0));
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setTimeTakenMinutes(submission.getTimeTakenMinutes());
        attempt.setStatus(com.eduapp.backend.model.AttemptStatus.SUBMITTED);

        // Calculate attempt number by counting existing attempts
        int existingAttempts = studentPaperAttemptRepository.countByStudentIdAndPaperId(userId, paperId);
        attempt.setAttemptNumber(existingAttempts + 1);

        StudentPaperAttempt savedAttempt = studentPaperAttemptRepository.save(attempt);

        // Save Answers
        if (submission.getAnswers() != null) {
            for (PaperSubmissionDto.StudentAnswerSubmissionDto ansDto : submission.getAnswers()) {
                Question question = questionRepository.findById(ansDto.getQuestionId())
                        .orElseThrow(
                                () -> new IllegalArgumentException("Question not found: " + ansDto.getQuestionId()));

                StudentAnswer answer = new StudentAnswer();
                answer.setAttempt(savedAttempt);
                answer.setQuestion(question);

                if (ansDto.getSelectedOptionId() != null) {
                    // MCQ: set selected option and populate answerText with option text
                    QuestionOption option = questionOptionRepository.findById(ansDto.getSelectedOptionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Option not found: " + ansDto.getSelectedOptionId()));
                    answer.setSelectedOption(option);
                    // Store the option text as answerText for display purposes
                    answer.setAnswerText(option.getText());
                } else {
                    // Text/Essay question: use the provided answer text
                    answer.setAnswerText(ansDto.getAnswerText());
                }

                studentAnswerRepository.save(answer);

                // Add to the attempt's list so it's available for AI analysis immediately
                // This avoids Hibernate cache issues where the list might appear empty
                savedAttempt.getAnswers().add(answer);
            }
        }

        logger.info("Loaded attempt with {} answers for AI analysis", savedAttempt.getAnswers().size());

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
     */
    public java.util.Map<Long, com.eduapp.backend.dto.PaperAttemptInfoDto> getAttemptInfoForPapers(
            List<Long> paperIds, Long userId) {
        logger.info("Getting attempt info for {} papers for user {}", paperIds.size(), userId);

        java.util.Map<Long, com.eduapp.backend.dto.PaperAttemptInfoDto> result = new java.util.HashMap<>();

        for (Long paperId : paperIds) {
            try {
                Paper paper = paperRepository.findById(paperId).orElse(null);
                if (paper == null) {
                    continue;
                }

                int attemptsMade = studentPaperAttemptRepository.countByStudentIdAndPaperId(userId, paperId);
                int freeAttempts = paper.getMaxFreeAttempts() != null ? paper.getMaxFreeAttempts() : 2;
                int extraAttempts = extraAttemptPurchaseRepository.sumExtraAttemptsByUserAndPaper(userId, paperId);
                int maxAttempts = freeAttempts + extraAttempts;
                int remainingAttempts = Math.max(0, maxAttempts - attemptsMade);
                boolean canAttempt = attemptsMade < maxAttempts;

                com.eduapp.backend.dto.PaperAttemptInfoDto info = new com.eduapp.backend.dto.PaperAttemptInfoDto(
                        paperId, attemptsMade, maxAttempts, remainingAttempts, canAttempt);

                result.put(paperId, info);
            } catch (Exception e) {
                logger.error("Error getting attempt info for paper {}: {}", paperId, e.getMessage());
            }
        }

        return result;
    }
}