package com.eduapp.backend.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed fixed-window rate limiter (WP-A1 / SCALE-13). Replaces the in-memory
 * Bucket4j limiter so per-user/per-IP limits hold across multiple backend instances.
 *
 * <p>Fixed 60-second window: INCR a counter keyed by (tier:caller); first hit sets a 60s
 * TTL. Fails OPEN if Redis is unavailable (a cache outage must not lock everyone out).
 */
@Component
public class RedisRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RedisRateLimiter.class);
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final StringRedisTemplate redis;

    public RedisRateLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public record Result(boolean allowed, long remaining, long retryAfterSeconds) {}

    /**
     * @param bucketKey       composite key, e.g. "auth:user:alice@x.com" or "general:ip:1.2.3.4"
     * @param limitPerMinute  max requests allowed in the 60s window
     */
    public Result tryConsume(String bucketKey, int limitPerMinute) {
        String key = "rl:" + bucketKey;
        try {
            Long count = redis.opsForValue().increment(key);
            if (count == null) {
                return new Result(true, -1, 0); // unexpected; don't block
            }
            if (count == 1L) {
                redis.expire(key, WINDOW);
            }
            boolean allowed = count <= limitPerMinute;
            long remaining = Math.max(0, limitPerMinute - count);
            long retryAfter = 0;
            if (!allowed) {
                Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
                retryAfter = (ttl == null || ttl < 0) ? WINDOW.toSeconds() : ttl;
            }
            return new Result(allowed, remaining, retryAfter);
        } catch (Exception e) {
            // Redis down → fail open so the platform stays available.
            logger.warn("Redis rate limiter unavailable, allowing request (fail-open): {}", e.getMessage());
            return new Result(true, -1, 0);
        }
    }
}
