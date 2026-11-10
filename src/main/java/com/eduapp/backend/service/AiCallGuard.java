package com.eduapp.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * SCALE-MARK-1: reusable bulkhead + circuit breaker for outbound AI calls.
 *
 * <p>The AI marking path ({@link AIAnalysisService}) calls Vertex/Gemini directly and previously
 * had no concurrency cap, no breaker and no budget — a Gemini slowdown could flood the async pool
 * with concurrent calls (429 storms + cost runaway). This guard:
 * <ul>
 *   <li><b>Bulkhead</b> — a bounded semaphore caps in-flight AI calls; callers fast-fail (instead
 *       of blocking unbounded) when full, so AI latency can't starve the executor.</li>
 *   <li><b>Circuit breaker</b> — after N consecutive failures the circuit opens and calls
 *       fast-fail for a cooldown; a single trial after the cooldown closes it again on success.</li>
 * </ul>
 *
 * <p>Mirrors the proven pattern already embedded in {@link AIService} for extraction, extracted
 * here so the marking path gets the same protection. Fast-fails throw {@link AiUnavailableException}
 * so callers can treat them as transient (ret*able) failures rather than poisoning a result.
 */
@Component
public class AiCallGuard {

    private static final Logger logger = LoggerFactory.getLogger(AiCallGuard.class);

    private final Semaphore slots;
    private final long acquireTimeoutMs;
    private final int cbFailureThreshold;
    private final long cbOpenMs;

    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private volatile long circuitOpenUntilMs = 0L;

    public AiCallGuard(
            @Value("${marking.ai.max-concurrent:8}") int maxConcurrent,
            @Value("${marking.ai.acquire-timeout-ms:2000}") long acquireTimeoutMs,
            @Value("${marking.ai.cb-failure-threshold:5}") int cbFailureThreshold,
            @Value("${marking.ai.cb-open-ms:30000}") long cbOpenMs) {
        this.slots = new Semaphore(Math.max(1, maxConcurrent));
        this.acquireTimeoutMs = acquireTimeoutMs;
        this.cbFailureThreshold = cbFailureThreshold;
        this.cbOpenMs = cbOpenMs;
    }

    /**
     * Run {@code call} under the bulkhead + circuit breaker.
     *
     * @throws AiUnavailableException if the circuit is open or the bulkhead is full (fast-fail)
     */
    public <T> T execute(Supplier<T> call) {
        if (System.currentTimeMillis() < circuitOpenUntilMs) {
            throw new AiUnavailableException("AI marking circuit is open — failing fast; retry shortly");
        }
        acquireSlot();
        try {
            T result = call.get();
            consecutiveFailures.set(0); // success closes the circuit
            return result;
        } catch (RuntimeException ex) {
            recordFailure();
            throw ex;
        } finally {
            slots.release();
        }
    }

    private void acquireSlot() {
        try {
            if (acquireTimeoutMs <= 0) {
                slots.acquire();
            } else if (!slots.tryAcquire(acquireTimeoutMs, TimeUnit.MILLISECONDS)) {
                throw new AiUnavailableException("AI marking is busy (bulkhead full) — please retry shortly");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiUnavailableException("Interrupted while waiting for an AI marking slot");
        }
    }

    private void recordFailure() {
        if (consecutiveFailures.incrementAndGet() >= cbFailureThreshold) {
            circuitOpenUntilMs = System.currentTimeMillis() + cbOpenMs;
            logger.error("AI marking circuit OPENED for {}ms after {} consecutive failures",
                    cbOpenMs, cbFailureThreshold);
        }
    }
}
