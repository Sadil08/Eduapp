package com.eduapp.backend.it;

import com.eduapp.backend.interceptor.RedisRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** WP-A1 — Redis-backed rate limiter allows up to the limit per window, then blocks. */
class RateLimitIT extends AbstractIntegrationTest {

    @Autowired RedisRateLimiter limiter;

    @Test
    void allowsUpToLimitThenBlocks() {
        String key = "test:" + UUID.randomUUID(); // unique → no cross-test interference
        int limit = 3;

        for (int i = 1; i <= limit; i++) {
            RedisRateLimiter.Result r = limiter.tryConsume(key, limit);
            assertThat(r.allowed()).as("request %d within limit", i).isTrue();
        }
        // (limit+1)-th request in the same window is rejected with a retry hint.
        RedisRateLimiter.Result over = limiter.tryConsume(key, limit);
        assertThat(over.allowed()).isFalse();
        assertThat(over.retryAfterSeconds()).isGreaterThan(0);
    }
}
