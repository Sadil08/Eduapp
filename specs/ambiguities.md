# Architectural ambiguities: backend

> **RESOLUTION STATUS (2026-06-14, branch `feature/multi-tenancy-school-tier`):** Resolved —
> `SecurityConfig` documents the filter chain + authorization rules; duplicate `@EnableAsync`
> removed from `BackendApplication` (AsyncConfig is the sole owner); `AsyncUncaughtExceptionHandler`
> is implemented; `AuthConfig` renamed to `PasswordEncoderConfig`; `RedisConfig` is the documented
> cache home (provider/named caches/TTL); the `core` diagnostics moved to `scripts/` with a README;
> `TestDB.java` (hardcoded creds) deleted; distributed rate-limit limitation documented in
> `RateLimitConfig`. See `TASKS.md` (WP-S) for per-item mapping.

## Summary
The backend service has several structural ambiguities that will slow onboarding and create fragility when adding features: the most critical are an effectively absent security model (only a PasswordEncoder bean exists with no chain or authorization rules), duplicate async configuration, and a core module whose name and contents are contradictory. Configuration discipline breaks down in at least one diagnostic file that hardcodes credentials, undermining the stated .env/env-var strategy.
## Ambiguities (11 found)
### [HIGH] security
**Locations:** src/main/java/com/eduapp/backend/config/AuthConfig.java, src (no SecurityFilterChain or WebSecurityConfigurerAdapter referenced)

AuthConfig declares only a BCryptPasswordEncoder bean. There is no visible security filter chain, no authentication mechanism, and no authorization rules anywhere in the documented architecture. A new engineer has no way to know which endpoints are protected, what roles exist, or how tokens/sessions are validated. This is either a critical gap or the security layer exists but is entirely undocumented — both outcomes are dangerous.

**Recommendation:** Create an explicit SecurityConfig class that declares the HttpSecurity filter chain, documents every endpoint's access rule (permitAll vs authenticated vs role-gated), and names the authentication mechanism (JWT, session, OAuth2, etc.). AuthConfig should either be merged into it or renamed to PasswordEncoderConfig to reflect its narrow scope.
### [HIGH] config
**Locations:** src/main/java/com/eduapp/backend/BackendApplication.java, src/main/java/com/eduapp/backend/config/AsyncConfig.java

@EnableAsync is declared on both BackendApplication and the dedicated AsyncConfig class. Spring will not throw an error — the annotation is idempotent — but the authoritative location for async configuration is ambiguous. A developer adding a new executor or tuning thread-pool parameters must know which class 'wins' conceptually, and a future refactor may strip one without realising the other also carries async semantics.

**Recommendation:** Remove @EnableAsync from BackendApplication. AsyncConfig is the correct single owner because it already defines the ThreadPoolTaskExecutor bean. Document in AsyncConfig's Javadoc that it is the sole async configuration point.
### [HIGH] config
**Locations:** core/TestDB.java

TestDB.java hardcodes the JDBC URL (localhost:5432/eduapp), username ('postgres'), and password ('password') inline. The stated configuration strategy is .env files + OS environment variables. This directly contradicts that strategy, makes the diagnostic tool useless in non-local environments, and creates a credentials-in-source-code risk if the file is committed to version control.

**Recommendation:** Rewrite TestDB.java to read connection parameters from environment variables or a .env file (using the same loading mechanism as the main service). Add the file to .gitignore or move it to a git-ignored scripts/ directory if it must remain in the repo.
### [HIGH] naming
**Locations:** core/TestDB.java, core/list_s3.py

The module named 'core' contains only two diagnostic/infrastructure-verification utilities (TestDB.java, list_s3.py). In Spring Boot convention and in general software architecture, 'core' implies shared domain abstractions, cross-cutting utilities, or foundational interfaces that other modules depend on. The actual content has nothing to do with that. A new engineer will look in 'core' for shared models or utilities and find operational scripts, and will look elsewhere for shared abstractions that may not exist.

**Recommendation:** Rename the module to 'scripts', 'tools', or 'infra-diagnostics'. If shared abstractions are ever needed across modules, create a true 'core' or 'common' module at that point with a clear charter.
### [MEDIUM] ownership
**Locations:** src/main/java/com/eduapp/backend/config/DatabaseMigrationRunner.java

DatabaseMigrationRunner is placed in the config package but is not a configuration class — it is a runtime operational component that executes DDL/DML on startup. The config package conventionally holds Spring @Configuration beans (infrastructure wiring). Mixing an imperative stateful runner into that package blurs the distinction between 'wiring the application' and 'running startup tasks', making it hard to find and reason about what the app does at boot.

**Recommendation:** Move DatabaseMigrationRunner to a dedicated package such as com.eduapp.backend.migration or com.eduapp.backend.startup. Evaluate whether a formal migration tool (Flyway or Liquibase) should own this responsibility instead, which would provide versioning, checksums, and rollback.
### [MEDIUM] error-handling
**Locations:** src/main/java/com/eduapp/backend/config/DatabaseMigrationRunner.java

DatabaseMigrationRunner implements CommandLineRunner, which means migration failures will surface as exceptions during application startup. It is undocumented whether a migration failure will abort startup (correct behavior) or be caught and swallowed, allowing the app to start against a potentially corrupt or out-of-date schema. There is also no documented rollback strategy, idempotency contract, or migration ordering guarantee.

**Recommendation:** Verify that exceptions from the run() method propagate and abort startup. Document explicitly in the class whether migrations are idempotent and what the expected schema baseline is. If this class grows beyond trivial DDL, replace it with Flyway/Liquibase.
### [MEDIUM] error-handling
**Locations:** src/main/java/com/eduapp/backend/config/AsyncConfig.java, src/main/java/com/eduapp/backend/BackendApplication.java

The architecture documents @EnableAsync with a configured ThreadPoolTaskExecutor but says nothing about what happens when an async task fails. Spring's default behavior for @Async methods is to silently discard exceptions unless an AsyncUncaughtExceptionHandler is configured. There is no mention of such a handler, meaning task failures may be swallowed with no logging, no alerting, and no retry.

**Recommendation:** Implement AsyncUncaughtExceptionHandler in AsyncConfig (by having it implement AsyncConfigurer) to at minimum log all unhandled async exceptions. Document the failure contract: are failed tasks retried, dead-lettered, or discarded?
### [MEDIUM] data-flow
**Locations:** core/list_s3.py, src (no S3 client or service class referenced in spec)

S3-compatible object storage integration is documented at two incompatible levels: the architecture spec says the Java service uses Supabase S3, and core/list_s3.py is a Python script using boto3. It is entirely unclear whether the main Java service has its own S3 client (AWS SDK for Java, Spring Cloud AWS, etc.), whether it shells out to the Python script, or whether the Python script is purely a developer diagnostic tool. The actual binary media upload/download code path in the Java service is invisible from the available documentation.

**Recommendation:** Document the Java-side S3 integration: which library is used, which service class owns upload/download, and what the URL/key structure is. Make clear in the core module README that list_s3.py is a standalone diagnostic script with no runtime dependency from the Java service.
### [MEDIUM] coupling
**Locations:** core/TestDB.java, core/list_s3.py

The core module is listed with affects: [] (no downstream dependencies), yet TestDB.java connects to the same database and list_s3.py connects to the same S3 bucket as the main service. If the DB schema or bucket configuration changes, the diagnostic tools break silently because the dependency is informal and untracked. This implicit environmental coupling means core can give false 'it works' signals if it is out of sync with the current schema.

**Recommendation:** Accept that core has an implicit runtime dependency on the same infrastructure as src and document it. Add a schema version check or at minimum a comment in TestDB.java indicating which table columns it expects, so drift is visible.
### [LOW] config
**Locations:** src/main/java/com/eduapp/backend/BackendApplication.java, src (no CacheConfig or @Cacheable usage documented)

Spring Cache is enabled (@EnableCaching) at bootstrap, but the architecture spec contains no documentation of which beans or methods are cached, what cache provider is in use (ConcurrentHashMap, Redis, Caffeine, etc.), what the eviction strategy is, or what the TTL of cached entries is. A new engineer adding a new read path has no model for when to add caching or how to size it.

**Recommendation:** Create a CacheConfig class that explicitly names the cache provider, declares named caches with TTL and size limits, and documents the eviction strategy. Add a section to the architecture spec listing which methods are @Cacheable and the rationale.
### [LOW] naming
**Locations:** src/main/java/com/eduapp/backend/config/AuthConfig.java

AuthConfig is named as if it configures authentication broadly, but its only content is a PasswordEncoder bean. If a proper security configuration class exists elsewhere in the 219-file src module, there are now two classes with auth-related names whose responsibilities overlap ambiguously. If no such class exists, the name overpromises the class's scope.

**Recommendation:** Rename to PasswordEncoderConfig or PasswordConfig to precisely describe its scope, or expand it to be the true authentication configuration home and rename SecurityConfig, pulling in the filter chain and related beans.
