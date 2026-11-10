package com.eduapp.backend.service;

import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    // NFR-DLQ-1: attempts older than MAX_AGE with no analysis are terminally failed (no more
    // retries). We stamp this sentinel + raise an ops alert so they aren't silently abandoned.
    static final String TERMINAL_ERROR_PREFIX = "[TERMINAL]";
    static final String TERMINAL_ERROR_MESSAGE =
            TERMINAL_ERROR_PREFIX + " Automated marking could not be completed after multiple attempts. "
                    + "Our team has been notified and will follow up.";

    // SCALE-2: a fixed key for the Postgres advisory lock that serialises the sweep across
    // horizontally-scaled instances (so only one runs at a time — no duplicate AI spend).
    private static final long SWEEP_LOCK_KEY = 4_270_010_001L;

    private final StudentPaperAttemptRepository attemptRepository;
    private final AIAnalysisService aiAnalysisService;

    @PersistenceContext
    private EntityManager entityManager;

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
        // SCALE-2: only one instance sweeps at a time. The transaction-scoped advisory lock is
        // auto-released at commit/rollback; if another instance holds it, skip this run.
        Boolean gotLock = (Boolean) entityManager
                .createNativeQuery("SELECT pg_try_advisory_xact_lock(:k)")
                .setParameter("k", SWEEP_LOCK_KEY)
                .getSingleResult();
        if (!Boolean.TRUE.equals(gotLock)) {
            logger.debug("MarkingSweeper: another instance holds the sweep lock; skipping this run");
            return 0;
        }

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

        // NFR-DLQ-1: anything past MAX_AGE with still no analysis is terminally failed — stop the
        // silent-abandon, record a terminal state and raise an ops alert (no more retries).
        markTerminalFailures(now);

        return stuck.size();
    }

    /**
     * NFR-DLQ-1: flag attempts that exceeded the retry window so they are not silently abandoned.
     * Idempotent via the {@code [TERMINAL]} sentinel (already-flagged attempts are excluded by the
     * query). Logs at ERROR so monitoring can alert + a human can replay them.
     */
    private void markTerminalFailures(LocalDateTime now) {
        List<StudentPaperAttempt> terminal = attemptRepository.findTerminallyFailed(
                now.minus(MAX_AGE), TERMINAL_ERROR_PREFIX + "%", PageRequest.of(0, BATCH));
        for (StudentPaperAttempt attempt : terminal) {
            attempt.setAnalysisError(TERMINAL_ERROR_MESSAGE);
            attempt.setAnalysisAttempted(true);
            attempt.setAnalysisCompleted(false);
            attemptRepository.save(attempt);
            logger.error("MarkingSweeper: attempt {} TERMINALLY FAILED marking (older than {}h, no analysis) "
                    + "— needs ops attention / manual replay", attempt.getId(), MAX_AGE.toHours());
        }
        if (!terminal.isEmpty()) {
            logger.error("MarkingSweeper: {} attempt(s) marked TERMINAL this run", terminal.size());
        }
    }
}
