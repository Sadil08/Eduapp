package com.eduapp.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process per-key rate limiting (Bucket4j).
 *
 * <p>NOTE (AMB-ratelimit-distributed): buckets live in a local {@link ConcurrentHashMap},
 * so limits are enforced PER INSTANCE. Across horizontally-scaled replicas the effective
 * limit is N× the configured value. For a multi-replica deployment, back Bucket4j with the
 * shared Redis already configured here (bucket4j-redis / lettuce) so limits are global, or
 * enforce limits at the reverse proxy / API gateway.
 */
@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // General API Limit: 60 requests per minute
    public Bucket resolveBucket(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::newBucket);
    }

    // Auth API Limit: 10 requests per minute (login, register)
    public Bucket resolveAuthBucket(String apiKey) {
        return cache.computeIfAbsent("auth_" + apiKey, this::newAuthBucket);
    }

    // Extraction API Limit: 5 requests per minute (heavy AI operations)
    public Bucket resolveExtractionBucket(String apiKey) {
        return cache.computeIfAbsent("extraction_" + apiKey, this::newExtractionBucket);
    }

    private Bucket newBucket(String apiKey) {
        Bandwidth limit = Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket newAuthBucket(String apiKey) {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket newExtractionBucket(String apiKey) {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
