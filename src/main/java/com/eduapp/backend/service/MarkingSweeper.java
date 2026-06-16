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
        return stuck.size();
    }
}
