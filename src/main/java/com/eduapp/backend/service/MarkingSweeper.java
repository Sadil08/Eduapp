package com.eduapp.backend.service;

import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * WP-A1 durable marking recovery. AI marking is dispatched via {@code @Async} and is lost
 * if the JVM dies mid-job, leaving a SUBMITTED attempt with no analysis forever. This
 * scheduled sweeper finds such stuck attempts and re-enqueues them — turning best-effort
 * in-JVM marking into an at-least-once, self-healing flow (a Postgres-backed outbox/queue
 * is the heavier future option; this covers the crash-recovery gap now).
 */
@Component
public class MarkingSweeper {

    private static final Logger logger = LoggerFactory.getLogger(MarkingSweeper.class);

    private static final Duration STALE_AFTER = Duration.ofMinutes(10);   // live async had its chance
    private static final Duration MAX_AGE = Duration.ofHours(24);          // don't retry ancient attempts
    private static final Duration RETRY_THROTTLE = Duration.ofMinutes(10); // gap between re-enqueues
    private static final int MAX_RETRIES = 5;
    private static final int BATCH = 20;

    private final StudentPaperAttemptRepository attemptRepository;
    private final AIAnalysisService aiAnalysisService;

    public MarkingSweeper(StudentPaperAttemptRepository attemptRepository, AIAnalysisService aiAnalysisService) {
        this.attemptRepository = attemptRepository;
        this.aiAnalysisService = aiAnalysisService;
    }

    @Scheduled(fixedDelayString = "${marking.sweeper.fixed-delay-ms:300000}",
            initialDelayString = "${marking.sweeper.initial-delay-ms:120000}")
    public void scheduledSweep() {
        int requeued = sweepOnce();
        if (requeued > 0) {
            logger.info("MarkingSweeper re-enqueued {} stuck attempt(s) for analysis", requeued);
        }
    }

    /**
     * Finds stuck attempts and re-enqueues analysis. Bookkeeping (lastSubmissionTime,
     * submissionCount) is committed before dispatch so a concurrent/next sweep won't pick
     * the same attempt again within the throttle window. Returns the number re-enqueued.
     */
    @Transactional
    public int sweepOnce() {
        LocalDateTime now = LocalDateTime.now();
        List<StudentPaperAttempt> stuck = attemptRepository.findStuckAnalysis(
                now.minus(STALE_AFTER), now.minus(MAX_AGE), now.minus(RETRY_THROTTLE),
                MAX_RETRIES, PageRequest.of(0, BATCH));

        for (StudentPaperAttempt attempt : stuck) {
            attempt.setLastSubmissionTime(now);
            attempt.setSubmissionCount((attempt.getSubmissionCount() == null ? 0 : attempt.getSubmissionCount()) + 1);
            attemptRepository.save(attempt);
            // Cross-bean call → @Async proxy applies → dispatched to the AI executor.
            aiAnalysisService.analyzeAttempt(attempt);
        }
        return stuck.size();
    }
}
