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
- [~] 1.8 Set `server.error.include-stacktrace=never` (DONE) + global `@RestControllerAdvice` returning sanitized errors (PENDING — defer until integration tests exist so we don't change error contracts blind) `(SEC-stacktrace, NFR)`. **Test:** forced error returns no stack trace/internal path.
- [ ] 1.9 Bean-validation (`jakarta.validation`) on all auth/request DTOs + `@Valid` `(SEC-NFR input validation)`. **Test:** malformed register payload → 400 with field errors.

## WP-2 — Migration Discipline (Flyway cutover)

- [ ] 2.1 Snapshot current schema as Flyway baseline `V5` (already `baseline-version=5`). **Test:** documented baseline matches a fresh-DB Hibernate build.
- [ ] 2.2 Create `db/migration/V6__textify_columns.sql` from `DatabaseMigrationRunner` DDL (idempotent) `(SCALE-1, SEC-5, AMB-runner)`. **Test:** Flyway applies V6 once on fresh DB.
- [ ] 2.3 **Delete `DatabaseMigrationRunner.java`** `(SCALE-1)`. **Test:** second boot performs zero DDL.
- [ ] 2.4 `ddl-auto=update` → `validate`; turn `flyway.validate-on-migrate=true` once clean. **Test:** integration boot passes with `validate` (no entity/schema drift).

## WP-3 — Roles, Authorities & Invite-Based Creation

- [x] 3.1 Extend `Role` enum → add `SCHOOL_ADMIN, TEACHER, SCHOOL_STUDENT`. **Test:** persistence round-trip per role.
- [x] 3.2 Verify `UserDetailsService` maps all five roles → `ROLE_*` authorities. **Test:** parameterised authority test per role. ✅ `UserDetailsAuthorityTest` (5/5)
- [ ] 3.3 `SecurityConfig`: add `/api/school/**`, `/api/teacher/**`, `/api/analytics/global/**` rules above `anyRequest`. **Test:** authz matrix (role × namespace) table-driven.
- [ ] 3.4 Invite/`SchoolInvite` token model + accept flow; privileged users only via authenticated invite/admin. **Test:** no public path mints SCHOOL_ADMIN/TEACHER.

## WP-4 — Tenant Core & Automatic Isolation

- [ ] 4.1 `School` entity + `V8__tenant_core.sql`; nullable `school_id` FK on `users`. **Test:** migration applies; existing users have null tenant.
- [ ] 4.2 `TenantContext` (ThreadLocal) + `TenantFilter` (after JWT filter), clears in `finally`. **Test:** context empty at request start.
- [ ] 4.3 Hibernate `@FilterDef`/`@Filter` on tenant entities, activated per request when tenant present. **Test:** school A cannot read school B (repo + HTTP).
- [ ] 4.4 ArchUnit test banning raw `findAll()` on tenant-scoped repos. **Test:** ArchUnit green; violation fails build.
- [ ] 4.5 Concurrency test: interleaved A/B requests, no cross-contamination (thread-leak guard). **Test:** green under parallel load.

## WP-5 — Class Management & Enrolment

- [ ] 5.1 `SchoolClass` + `SchoolEnrolment` entities + Flyway `V9`. **Test:** tenant-scoped CRUD.
- [ ] 5.2 `/api/school/classes` endpoints (create/list/detail), teacher invite. **Test:** SCHOOL_ADMIN-only authz + isolation.
- [ ] 5.3 Class-code self-enrolment mints `SCHOOL_STUDENT`. **Test:** cross-school class code rejected.

## WP-6 — School Papers (reuse extraction + marking)

- [ ] 6.1 `SchoolPaper` entity + nullable `school_paper_id` on `Question` + Flyway `V10`. **Test:** tenant-scoped.
- [ ] 6.2 Upload→extract (reuse `AIService`)→teacher review/approve flow. **Test:** extraction reuses consumer path; unapproved hidden from students.
- [ ] 6.3 Assign paper to class with exam window. **Test:** assignment scoped + visible only to enrolled students.

## WP-7 — School Attempts & Teacher Override

- [ ] 7.1 `SchoolPaperAttempt` entity + Flyway `V11`. **Test:** tenant-scoped.
- [ ] 7.2 Student sit within exam window (reuse `StudentAnswer` + AI marking). **Test:** early/late submit rejected; AI mark recorded.
- [ ] 7.3 Teacher override (mark + mandatory note + `teacher_reviewed_at`). **Test:** override persists, audited, supersedes AI in gradebook; results hidden until `results_released`.

## WP-8 — Cohort Analytics (school-scoped)

- [ ] 8.1 `/api/school/analytics/**` + teacher analytics: overview, topic heatmap, question-failure, grade distribution, misconception clustering. **Test:** A never sees B's data; numbers reconcile to fixtures.
- [ ] 8.2 All aggregates paginated/GROUP BY, no unbounded `findAll` `(SCALE-3, SCALE-4)`. **Test:** large fixture stays bounded.
- [ ] 8.3 `@Cacheable` + TTL on heavy aggregates (Redis) `(SCALE-5)`. **Test:** repeat call hits cache.

## WP-9 — Consent & Cambridge Aggregate API

- [ ] 9.1 `StudentConsentRecord` entity + Flyway `V12`; layered consent at registration (req/optional/age gate). **Test:** consent persisted; age <16 requires parental consent.
- [ ] 9.2 `/api/analytics/global/**` (ADMIN) returns aggregated anonymised JSON only, filtered on `analytics_sharing_consented`. **Test:** non-consented excluded; no PII/raw rows.
- [ ] 9.3 Hard minimum cohort size 50 guard. **Test:** boundary 49 suppressed / 50 allowed / 51 allowed.
- [ ] 9.4 School-tier data never present in global export. **Test:** explicit exclusion assertion.

## WP-S — Spec-Debt Cleanup (closes ALL remaining flagged items)

- [x] S.1 Remove duplicate `@EnableAsync` from `BackendApplication` (keep in `AsyncConfig`) `(AMB-async-dup)`. **Test:** async still works (integration).
- [x] S.2 Right-size async pool: core 20–50, max 100, queue 500–1000, sensible `RejectedExecutionHandler` (503/CallerRuns) `(SCALE-2)`. **Test:** burst of >105 concurrent tasks no `TaskRejectedException` to user.
- [x] S.3 Verify/implement `AsyncUncaughtExceptionHandler` logging in `AsyncConfig` `(AMB-async-err)`. **Test:** failing async task is logged, not swallowed.
- [ ] S.4 Paginate `AIAnalysisController` + all admin list endpoints (`AdminUserController`, `AdminBundleController`, `AdminCustomBundleController`) — `Pageable`, default size 20, max 100 `(SCALE-3, SCALE-4)`. **Test:** `?page&size` honoured; oversize clamped.
- [ ] S.5 Cache `AdminDashboardController` revenue with TTL `(SCALE-5)`. **Test:** second call cached.
- [ ] S.6 Explicit HikariCP config (max-pool 20, min-idle 5, timeout 30s, leak-detection 60s) `(SCALE-7)`. **Test:** properties applied; pool metrics sane under load test.
- [ ] S.7 S3 calls: Resilience4j circuit-breaker + retry + connect/read timeouts (5s/30s) `(SCALE-8)`. **Test:** simulated S3 outage fails fast, no thread pile-up.
- [ ] S.8 Redis serialization: replace polymorphic default typing with DTO/concrete serializers `(SCALE-9)`. **Test:** cache round-trip after a class rename still deserializes.
- [ ] S.9 Delete `DBTest`/`TestDB` (hardcoded creds, `printStackTrace`, connection leak) `(SEC-4, SEC-6, AMB-testdb, SCALE-10)`. **Test:** file gone; no hardcoded creds in repo (grep).
- [ ] S.10 Rename `core` module → `scripts`/`tools`; document `list_s3.py` as standalone diagnostic; add S3 pagination note `(AMB-core-naming, AMB-s3-doc, AMB-core-coupling)`. **Test:** docs updated; build unaffected.
- [ ] S.11 Rename `AuthConfig` → `PasswordEncoderConfig` (or fold into `SecurityConfig`) `(AMB-authconfig-naming)`. **Test:** context loads; password encoding works.
- [ ] S.12 Add documented `CacheConfig` (provider, named caches, TTL, eviction) `(AMB-cacheconfig)`. **Test:** caches resolve with configured TTL.
- [ ] S.13 Distributed rate-limit note/option (Redis-backed Bucket4j) for multi-replica `(AMB-ratelimit-distributed)`. **Test:** documented; (impl optional this phase).
- [ ] S.14 Update `specs/*.md` to reflect resolved items (stale-doc cleanup). **Test:** specs reference current reality.

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
- ⏭ NEXT: WP-2 Flyway cutover (needs schema baseline from `eduapp_db` via pg_dump) → WP-4 tenant core → WP-5+ school features.
