package com.eduapp.backend.service;

import com.eduapp.backend.exception.ExtractionLimitExceededException;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionExtractionTracking;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.repository.QuestionExtractionTrackingRepository;
import com.eduapp.backend.repository.QuestionRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing extraction tracking and enforcing limits.
 * Prevents API abuse by limiting image extractions per question per attempt.
 */
@Service
public class ExtractionTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(ExtractionTrackingService.class);

    private final QuestionExtractionTrackingRepository trackingRepository;
    private final StudentPaperAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;

    public ExtractionTrackingService(
            QuestionExtractionTrackingRepository trackingRepository,
            StudentPaperAttemptRepository attemptRepository,
            QuestionRepository questionRepository) {
        this.trackingRepository = trackingRepository;
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Check if extraction is allowed for this question in this attempt
     * 
     * @param attemptId  Student paper attempt ID
     * @param questionId Question ID
     * @return true if extraction is allowed, false otherwise
     */
    public boolean canExtract(Long attemptId, Long questionId) {
        QuestionExtractionTracking tracking = getOrCreateTracking(attemptId, questionId);
        return tracking.canExtract();
    }

    /**
     * NFR-RACE-1: atomically reserve one extraction slot BEFORE the (paid) AI call. Replaces the
     * old check-then-act {@code recordExtraction}, which let concurrent requests both pass the
     * {@code canExtract} check and exceed the cap (extra AI cost). On limit, throws and no slot is
     * consumed. If the subsequent AI call fails, the caller MUST call
     * {@link #releaseExtraction} so the student isn't penalised for an outage.
     *
     * @throws ExtractionLimitExceededException if the per-question limit is already reached
     */
    @Transactional
    public void reserveExtraction(Long attemptId, Long questionId) {
        QuestionExtractionTracking tracking = getOrCreateTracking(attemptId, questionId);
        int reserved = trackingRepository.tryIncrementCount(tracking.getId());
        if (reserved == 0) {
            int used = tracking.getExtractionCount();
            int max = tracking.getMaxExtractions();
            logger.warn("Extraction limit exceeded for attempt {} question {}: {}/{}",
                    attemptId, questionId, used, max);
            throw new ExtractionLimitExceededException(
                    String.format("Extraction limit reached for this question (%d/%d used)", used, max),
                    used, max);
        }
        logger.info("Reserved extraction slot (atomic) for attempt {} question {}", attemptId, questionId);
    }

    /**
     * NFR-RACE-1: release a slot reserved by {@link #reserveExtraction} when the AI extraction call
     * fails, so a transient AI/system error does not burn the student's quota.
     */
    @Transactional
    public void releaseExtraction(Long attemptId, Long questionId) {
        trackingRepository.findByAttemptIdAndQuestionId(attemptId, questionId).ifPresent(t -> {
            if (trackingRepository.decrementCount(t.getId()) > 0) {
                logger.info("Released extraction reservation for attempt {} question {} (AI call failed)",
                        attemptId, questionId);
            }
        });
    }

    /**
     * Get remaining extractions for this question
     * 
     * @param attemptId  Student paper attempt ID
     * @param questionId Question ID
     * @return Number of remaining extraction attempts
     */
    public int getRemainingExtractions(Long attemptId, Long questionId) {
        QuestionExtractionTracking tracking = getOrCreateTracking(attemptId, questionId);
        return tracking.getRemainingExtractions();
    }

    /**
     * Get extraction count for this question
     * 
     * @param attemptId  Student paper attempt ID
     * @param questionId Question ID
     * @return Number of extractions used
     */
    public int getExtractionCount(Long attemptId, Long questionId) {
        QuestionExtractionTracking tracking = getOrCreateTracking(attemptId, questionId);
        return tracking.getExtractionCount();
    }

    /**
     * Get all extraction counts for an attempt
     * Returns map of questionId -> extractionsUsed
     * 
     * @param attemptId Student paper attempt ID
     * @return Map of question IDs to extraction counts
     */
    public Map<Long, Integer> getAllExtractionCounts(Long attemptId) {
        List<QuestionExtractionTracking> trackings = trackingRepository.findByAttemptId(attemptId);
        Map<Long, Integer> counts = new HashMap<>();

        for (QuestionExtractionTracking tracking : trackings) {
            counts.put(tracking.getQuestion().getId(), tracking.getExtractionCount());
        }

        return counts;
    }

    /**
     * Initialize tracking for all questions when attempt starts
     * Creates tracking records with 0 extractions for each question
     * 
     * @param attempt Student paper attempt
     */
    @Transactional
    public void initializeTrackingForAttempt(StudentPaperAttempt attempt) {
        logger.info("Initializing extraction tracking for attempt {}, paper {}",
                attempt.getId(), attempt.getPaper().getId());

        List<Question> questions = attempt.getPaper().getQuestions();

        for (Question question : questions) {
            // Check if tracking already exists (shouldn't happen, but safe)
            if (trackingRepository.findByAttemptIdAndQuestionId(attempt.getId(), question.getId()).isEmpty()) {
                QuestionExtractionTracking tracking = new QuestionExtractionTracking(attempt, question);
                trackingRepository.save(tracking);
                logger.debug("Created tracking for question {}", question.getId());
            }
        }

        logger.info("Initialized tracking for {} questions", questions.size());
    }

    /**
     * Get or create tracking record for a specific attempt and question
     * 
     * @param attemptId  Student paper attempt ID
     * @param questionId Question ID
     * @return Tracking record
     */
    private QuestionExtractionTracking getOrCreateTracking(Long attemptId, Long questionId) {
        Optional<QuestionExtractionTracking> existing =
                trackingRepository.findByAttemptIdAndQuestionId(attemptId, questionId);
        if (existing.isPresent()) {
            return existing.get();
        }
        logger.warn("Tracking not found for attempt {} question {}, creating lazily", attemptId, questionId);
        StudentPaperAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));
        try {
            return trackingRepository.saveAndFlush(new QuestionExtractionTracking(attempt, question));
        } catch (org.springframework.dao.DataIntegrityViolationException race) {
            // NFR-RACE-1: a concurrent request created the row first (unique constraint on
            // attempt+question). Re-fetch the winner rather than creating a duplicate.
            logger.debug("Concurrent tracking creation for attempt {} question {}; re-fetching",
                    attemptId, questionId);
            return trackingRepository.findByAttemptIdAndQuestionId(attemptId, questionId)
                    .orElseThrow(() -> race);
        }
    }

    /**
     * Get total extraction count for analytics
     * 
     * @return Total number of extractions across all users
     */
    public Long getTotalExtractionCount() {
        return trackingRepository.sumAllExtractionCounts();
    }

    /**
     * Get total extraction count for a specific user
     * 
     * @param userId User ID
     * @return Total number of extractions for this user
     */
    public Long getUserExtractionCount(Long userId) {
        return trackingRepository.sumExtractionCountsByUserId(userId);
    }
}
