package com.eduapp.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Cache configuration and the documented cache home (AMB-cacheconfig).
 *
 * <p>Provider: Redis ({@link RedisCacheManager}). Default TTL 10 min; named caches have
 * explicit TTLs. Eviction is TTL-based.
 * <ul>
 *   <li>{@code revenueCache} — total-revenue SUM, 60s TTL (SCALE-5).</li>
 *   <li>{@code schoolPaperSummary} — cohort analytics, 5 min TTL (WP-8); key includes the
 *       tenant id so a cache hit can never serve another school's data.</li>
 * </ul>
 *
 * <p>SCALE-9: default typing is required to round-trip concrete DTOs through
 * {@code @Cacheable}, but the polymorphic type validator is restricted to this app's own
 * packages + base JDK types (instead of the permissive default) to avoid gadget risk.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator ptv =
                com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.eduapp.backend.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.time.")
                        .allowIfSubType("java.math.")
                        .allowIfSubType("java.lang.")
                        .build();
        objectMapper.activateDefaultTyping(ptv,
                com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        Map<String, RedisCacheConfiguration> perCache = Map.of(
                "revenueCache", defaults.entryTtl(Duration.ofSeconds(60)),
                "schoolPaperSummary", defaults.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}
