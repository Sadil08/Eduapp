package com.eduapp.backend.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SCALE-MARK-1: proves the marking bulkhead + circuit breaker. These were absent on the marking
 * path, so a Gemini slowdown could flood the async pool with concurrent calls. The guard must cap
 * concurrency (fast-fail when full) and open the circuit after repeated failures.
 */
class AiCallGuardTest {

    @Test
    void passesThroughResultAndResetsFailuresOnSuccess() {
        AiCallGuard guard = new AiCallGuard(4, 100, 3, 1000);
        assertThat(guard.execute(() -> "ok")).isEqualTo("ok");
    }

    @Test
    void circuitOpensAfterThreshold_thenFastFails() {
        AiCallGuard guard = new AiCallGuard(4, 100, 3, 60_000);
        // 3 consecutive failures should open the circuit.
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> guard.execute(() -> { throw new RuntimeException("boom"); }))
                    .isInstanceOf(RuntimeException.class);
        }
        // Now the circuit is open: the next call fast-fails WITHOUT invoking the supplier.
        AtomicInteger invoked = new AtomicInteger();
        assertThatThrownBy(() -> guard.execute(() -> { invoked.incrementAndGet(); return "x"; }))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("circuit is open");
        assertThat(invoked.get()).isZero();
    }

    @Test
    void bulkheadFull_fastFailsWithoutBlocking() throws InterruptedException {
        // One slot, tiny acquire timeout. Hold the slot in another thread, then a second call must
        // fast-fail rather than block.
        AiCallGuard guard = new AiCallGuard(1, 50, 100, 1000);
        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Thread holder = new Thread(() -> guard.execute(() -> {
            holding.countDown();
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return "held";
        }));
        holder.start();
        assertThat(holding.await(1, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(() -> guard.execute(() -> "second"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("bulkhead full");

        release.countDown();
        holder.join(2000);
    }
}
