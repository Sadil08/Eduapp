package com.eduapp.backend.service;

import com.eduapp.backend.model.OverallPaperAnalysis;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.repository.OverallPaperAnalysisRepository;
import com.eduapp.backend.repository.StudentAnswerRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NFR-TXN-1 / NFR-IDEM-1: the transactional half of AI marking, kept in a separate bean so the
 * short DB transactions here run via the Spring proxy and are NOT held open across the slow
 * external Gemini call (which {@link AIAnalysisService} performs between
 * {@link #buildPromptForAttempt} and {@link #persistMarking}).
 *
 * <ul>
 *   <li>{@link #buildPromptForAttempt} — read-only txn; loads the attempt + answers and renders
 *       the prompt while lazy associations are still attached.</li>
 *   <li>{@link #persistMarking} — write txn; <b>upserts</b> the {@link OverallPaperAnalysis} (so a
 *       re-run never creates a duplicate — NFR-IDEM-1), clamps marks, and flips the attempt's
 *       completion flag. Throws on incomplete coverage so a partial model response is retried
 *       rather than silently under-counted (NFR-PARTIAL-1).</li>
 *   <li>{@link #recordError} — write txn; records a failure state for the frontend/retry path.</li>
 * </ul>
 */
@Service
public class MarkingPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(MarkingPersistenceService.class);

    private final StudentPaperAttemptRepository attemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final OverallPaperAnalysisRepository analysisRepository;

    public MarkingPersistenceService(StudentPaperAttemptRepository attemptRepository,
            StudentAnswerRepository studentAnswerRepository,
            OverallPaperAnalysisRepository analysisRepository) {
        this.attemptRepository = attemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.analysisRepository = analysisRepository;
    }

    /** Read-only: load attempt + answers and render the marking prompt (lazy access happens here). */
    @Transactional(readOnly = true)
    public String buildPromptForAttempt(Long attemptId) {
        StudentPaperAttempt attempt = attemptRepository.findByIdWithAnswers(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
        logger.info("Building AI prompt for attempt {} with {} answers", attemptId, attempt.getAnswers().size());
        return MarkingPromptBuilder.build(attempt);
    }

    /**
     * Apply the parsed marks. Upsert (find-or-create) the analysis so repeated dispatch (live async
     * + sweeper + manual retry) yields exactly one row. Throws {@link IllegalStateException} if the
     * model did not return every question — the caller treats that as a retryable failure.
     */
    @Transactional
    public void persistMarking(Long attemptId, MarkingResult result) {
        StudentPaperAttempt attempt = attemptRepository.findByIdWithAnswers(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
        List<StudentAnswer> answers = attempt.getAnswers();

        Set<Long> paperQuestionIds = new HashSet<>();
        int totalObtained = 0;
        int totalAllocated = 0;

        for (StudentAnswer answer : answers) {
            Long qId = answer.getQuestion().getId();
            paperQuestionIds.add(qId);
            int maxMarks = answer.getQuestion().getMarks() != null ? answer.getQuestion().getMarks() : 0;
            totalAllocated += maxMarks;

            MarkingResult.QuestionMark qm = result.byQuestionId().get(qId);
            if (qm != null) {
                // SEC-3: never trust the model's number — clamp to [0, maxMarks].
                int marks = AIAnalysisService.clampMarks(qm.rawMarks(), maxMarks);
                if (marks != qm.rawMarks()) {
                    logger.warn("SEC-3: AI returned out-of-range marks {} for question {} (max {}); clamped to {}",
                            qm.rawMarks(), qId, maxMarks, marks);
                }
                answer.setMarksAwarded(marks);
                answer.setAiFeedback(qm.feedback());
                studentAnswerRepository.save(answer);
                totalObtained += marks;
            }
        }

        // NFR-PARTIAL-1: require full coverage. A missing question would silently under-count the
        // weighted score (denominator still counts it), so treat partial output as a retryable
        // failure rather than persisting a wrong total. Rolls back the answer saves above.
        if (!result.returnedQuestionIds().containsAll(paperQuestionIds)) {
            throw new IllegalStateException(String.format(
                    "AI returned partial marking for attempt %d: %d/%d questions covered — will retry",
                    attemptId, result.returnedQuestionIds().size(), paperQuestionIds.size()));
        }

        // NFR-IDEM-1: upsert — reuse the existing row if marking ran before.
        OverallPaperAnalysis analysis = analysisRepository.findByAttemptId(attemptId)
                .orElseGet(OverallPaperAnalysis::new);
        analysis.setAttempt(attempt);
        if (result.overallFeedback() != null) {
            analysis.setOverallFeedback(result.overallFeedback());
        }

        Integer paperTotalMarks = attempt.getPaper().getTotalMarks();
        if (paperTotalMarks != null && totalAllocated > 0) {
            double fraction = (double) totalObtained / totalAllocated;
            analysis.setTotalMarks((int) Math.round(fraction * paperTotalMarks));
        } else if (result.aiTotalMarks() != null) {
            analysis.setTotalMarks(result.aiTotalMarks());
        }
        analysisRepository.save(analysis);

        // NFR-FLAG-1: success is now positively recorded (the flag was previously never set true).
        attempt.setAnalysisCompleted(true);
        attempt.setAnalysisAttempted(true);
        attempt.setAnalysisError(null);
        attemptRepository.save(attempt);

        logger.info("AI analysis persisted for attempt {} (obtained={}, allocated={})",
                attemptId, totalObtained, totalAllocated);
    }

    /** Record a failure so the frontend can show a retry and the sweeper can recover it. */
    @Transactional
    public void recordError(Long attemptId, String message) {
        attemptRepository.findById(attemptId).ifPresent(attempt -> {
            attempt.setAnalysisError(message);
            attempt.setAnalysisAttempted(true);
            attempt.setAnalysisCompleted(false);
            attemptRepository.save(attempt);
            logger.info("Recorded analysis error for attempt {}: {}", attemptId, message);
        });
    }
}
