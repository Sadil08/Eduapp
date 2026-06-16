package com.eduapp.backend.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SEC-5 — on a Redis outage the limiter fails OPEN for non-sensitive tiers (availability) but
 * fails CLOSED when the caller asks for it (auth/extraction), so an outage can't open a
 * brute-force / cost-abuse window.
 */
class RedisRateLimiterTest {

    @Test
    void failsOpen_byDefault_whenRedisDown() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenThrow(new RuntimeException("redis down"));
        RedisRateLimiter limiter = new RedisRateLimiter(redis);

        assertThat(limiter.tryConsume("general:ip:1.2.3.4", 100).allowed()).isTrue();
    }

    @Test
    void failsClosed_whenRequested_andRedisDown() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenThrow(new RuntimeException("redis down"));
        RedisRateLimiter limiter = new RedisRateLimiter(redis);

        RedisRateLimiter.Result r = limiter.tryConsume("auth:ip:1.2.3.4", 5, false);
        assertThat(r.allowed()).isFalse();
        assertThat(r.retryAfterSeconds()).isGreaterThan(0);
    }
}
