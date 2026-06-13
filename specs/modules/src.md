---
module: src
affects: []
files: ['src/main/java/com/eduapp/backend/BackendApplication.java', 'src/main/java/com/eduapp/backend/config/AsyncConfig.java', 'src/main/java/com/eduapp/backend/config/AuthConfig.java', 'src/main/java/com/eduapp/backend/config/DatabaseMigrationRunner.java', 'src/main/java/com/eduapp/backend/config/JacksonConfig.java', 'src/main/java/com/eduapp/backend/config/RateLimitConfig.java', 'src/main/java/com/eduapp/backend/config/RedisConfig.java', 'src/main/java/com/eduapp/backend/config/SecurityConfig.java', 'src/main/java/com/eduapp/backend/config/SupabaseStorageConfig.java', 'src/main/java/com/eduapp/backend/config/WebConfig.java']
---

# Src Module

## Purpose
This module is the root of the `com.eduapp.backend` Spring Boot application. It bootstraps the application via `BackendApplication` and houses all cross-cutting infrastructure configuration (security, async execution, caching, serialization, rate limiting, and database migration) required by the rest of the system.

## Public interfaces
- **`BackendApplication.main(String[])`** — application entry point; starts the Spring context with async and caching enabled via `@EnableAsync` and `@EnableCaching`.
- **`AsyncConfig.getAsyncExecutor()`** — exposes a `ThreadPoolTaskExecutor` (core=2, max=5, queue=100, thread prefix `"async-ai-"`) as the default `@Async` executor.
- **`AuthConfig.passwordEncoder()`** — exposes a `BCryptPasswordEncoder` bean for password hashing throughout the application.
- **`RateLimitConfig.resolveBucket(String)`**, **`resolveAuthBucket(String)`**, **`resolveExtractionBucket(String)`** — return per-key `Bucket` instances enforcing 60 req/min (general), 10 req/min (auth), and 5 req/min (extraction) limits respectively, stored in a `ConcurrentHashMap`.
- **`DatabaseMigrationRunner.run(String...)`** — `CommandLineRunner` that executes DDL `ALTER TABLE` statements at startup to widen `questions.text` and `questions.image_url` columns to `TEXT`.
- **`JacksonConfig.hibernate6Module()`** — registers `Hibernate6Module` with `FORCE_LAZY_LOADING=false` to prevent `ByteBuddyInterceptor` serialization errors on uninitialized lazy associations.
- **`RedisConfig.cacheManager(RedisConnectionFactory)`** — produces a `RedisCacheManager` with JSON serialization for cache values.
- **`SecurityConfig`** — configures the `SecurityFilterChain`, installs `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`, and sets session policy to stateless.

## Data flow
- **Inbound requests** pass through `SecurityConfig`'s filter chain; `JwtAuthenticationFilter` validates the JWT before any controller is reached. Rate-limiting buckets in `RateLimitConfig` are consulted per API key (key sourced from the HTTP request by the caller).
- **Async operations** (e.g., AI calls) are dispatched to the `ThreadPoolTaskExecutor` defined in `AsyncConfig`; results flow back via Spring's `@Async` future mechanism.
- **Cache reads/writes** are handled transparently by `RedisCacheManager`; values are serialized to/from JSON via `GenericJackson2JsonRedisSerializer`.
- **Startup migration** — `DatabaseMigrationRunner` issues DDL directly through `JdbcTemplate` before the application accepts traffic.

## Architecture principles
- The application is **stateless at the HTTP layer** — `SecurityConfig` enforces `SessionCreationPolicy.STATELESS`.
- **Async thread pool is intentionally small** (max 5 threads) to cap concurrent AI/heavy operations; the queue depth of 100 acts as a backpressure buffer.
- **Rate limiting is in-process and per-key** using Bucket4j `ConcurrentHashMap`; there is no distributed rate-limit store, so limits are per-instance only.
- **Schema migrations run on every startup** via `DatabaseMigrationRunner`; DDL statements are not idempotent guards — they will fail if the column type is already `TEXT` on subsequent runs (no `IF NOT EXISTS` protection visible).
- **Lazy-loaded Hibernate associations must never be serialized** — `Hibernate6Module` is configured to replace uninitialized proxies with `null` rather than throwing.

## Dependencies
- **Internal:** `com.eduapp.backend.security.JwtAuthenticationFilter` (consumed by `SecurityConfig`)
- **External:** Spring Boot (core, security, web, data-redis), Spring Cache, Spring JDBC (`JdbcTemplate`), Hibernate 6 / `jackson-datatype-hibernate6`, Jackson Databind, Bucket4j (`io.github.bucket4j`), SLF4J, BCrypt (`spring-security-crypto`)

## Known gaps / TODOs
- `AsyncConfig`, `JacksonConfig`, `RateLimitConfig`, `RedisConfig`, and `SecurityConfig` are all truncated in the provided source — the `AsyncUncaughtExceptionHandler` implementation, full `RedisCacheManager` TTL configuration, CORS origins, and permitted URL patterns are unknown.
- `SupabaseStorageConfig` and `WebConfig` are listed as files but no source was provided; their behaviour is entirely undocumented.
- `DatabaseMigrationRunner` DDL statements lack idempotency guards (e.g., `DO $$ BEGIN … EXCEPTION WHEN … END $$`) — repeated startup will throw if columns are already `TEXT`.
- In-process rate-limit buckets in `RateLimitConfig` are not shared across horizontal replicas; distributed deployments will not enforce global limits.