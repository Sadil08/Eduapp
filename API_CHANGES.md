# API Changes — N-SC2 (list-endpoint scaling + authorization hardening)

This is the authoritative log of **breaking and security-affecting changes** to list
endpoints, made under audit item **N-SC2** (unbounded `findAll()`) and the companion
authorization review. Each row records the endpoint, what it did **before**, what it does
**after**, and the **frontend impact** so the Next.js client can be updated to look unchanged.

Legend: ☐ not started · ◐ backend done, frontend pending · ☑ backend + frontend done.

---

## A. Authorization / ownership fixes (security — may change *which rows* are returned)

Root cause: `SecurityConfig` ends with `anyRequest().authenticated()`, so any logged-in user
(any role) could call these and receive **all users' rows**. Fixes scope each to the caller,
a role, or remove the endpoint where the frontend never used it.

| # | Endpoint | Before (auth → returns) | After (auth → returns) | Frontend impact | Status |
|---|----------|-------------------------|------------------------|-----------------|--------|
| A1 | `GET /api/progress` | any authed → **all** progress (the `?userId=` param was *ignored*) | authed → **caller's own** progress (`userId` from JWT principal) | `progressService.getProgress(userId)` already expects the user's rows — **no change needed**, bug fixed | ☑ |
| A2 | `GET /api/notifications` | any authed → **all** notifications | authed → **caller's own** notifications | no frontend caller — safe | ☑ |
| A3 | `GET /api/carts` | any authed → **all** carts | authed → **caller's own** cart(s) | `cartService.getCart()` reads `response.data[0]` — still works (returns caller's cart) | ☑ |
| A4 | `GET /api/question-options` | any authed → **all** options | `ADMIN` (class-level) | no frontend caller — safe | ☑ |
| A5 | `GET /api/overall-paper-analyses` | any authed → all | `ADMIN` (class-level) | no frontend caller — safe | ☑ |
| A6 | `GET /api/questions` (`getAllQuestions`) | any authed → **all** questions | `ADMIN` + paginated `Page<QuestionDto>` (see B4) | no frontend caller (`/questions/import-from-pdf`, `/extract-batch` only) — safe | ☑ |

Backend verified by `NSC2AuthorizationIT` (own-data scoping for notifications/carts/progress; ADMIN-lock + Page shape for questions) and the updated `QuestionControllerTest`.

Also flagged for the entity-body CRUD controllers (Cart/Notification/Progress/QuestionOption/
OverallPaperAnalysis): `GET/PUT/DELETE /{id}` and entity-typed `POST`/`PUT` bodies have **no
ownership check** — any authed user can read/mutate any row by id. Tracked under N-S9
(entity→DTO) + N-SC2-A ownership; addressed alongside the list fixes where cheap.

## B. Pagination conversions (scalability — response **shape** changes List → Page)

`Page<T>` JSON shape: `{ content: [...], totalElements, totalPages, number, size, ... }`.
Frontend must read `.content` (and optionally page metadata) instead of treating the body as a
bare array. Query params: `?page` (0-based), `?size` (capped server-side), optional `?q`.

| # | Endpoint | Before | After | Frontend impact | Status |
|---|----------|--------|-------|-----------------|--------|
| B1 | `GET /api/papers` (admin all) | `List<PaperDto>` (ADMIN) | `Page<PaperDto>` `?page&size&q` | FE: `paperService.getBundlePapers` repointed to `GET /api/paper-bundles/{id}` `.papers` (fixes a pre-existing bug — it had passed an ignored `bundleId` to the admin endpoint). `/api/papers/available` kept (deprecated) hard-capped at 500. | ☑ |
| B2 | `GET /api/admin/bundles` | `List<AdminBundleDto>` | `Page<AdminBundleDto>` `?page&size` | FE: `adminService.getBundles()` requests `size=100` and unwraps `.content`, still returns a flat array → 3 admin pages unchanged | ☑ |
| B3 | `GET /api/paper-bundles` / `/summaries` (public catalog) | `List<...>` | _(deferred)_ | **DEFERRED** — public storefront; converting to `Page` is a UX decision (pages vs infinite-scroll) and needs frontend verification. Recommend a follow-up with the catalog UI. | ☐ |
| B4 | `GET /api/questions` | `List<QuestionDto>` | `Page<QuestionDto>` `?page&size` (ADMIN, see A6) | none (no caller) | ☑ |

Backend for B1/B2/B4 verified by `NSC2AuthorizationIT` (Page shape + admin lock).

| B5 | `GET /api/question-options` | `List<QuestionOptionDto>` | `Page<QuestionOptionDto>` (ADMIN) | none (no caller) | ☑ |
| B6 | `GET /api/student-bundle-access` | `List<StudentBundleAccess>` | `Page<StudentBundleAccess>` (ADMIN) | none (no caller) | ☑ |
| B7 | `GET /api/overall-paper-analyses` | `List<OverallPaperAnalysis>` | `Page<OverallPaperAnalysis>` (ADMIN) | none (no caller) | ☑ |

Reference tables left **as full lists** (naturally bounded, low churn): `GET /api/subjects`,
`/api/lessons`, `/api/exam-types`. Allow-listed in the ArchUnit guard.

**Dead unbounded `findAll()` removed (no callers, no shape change):** `PaperService.findAll`,
`QuestionService.findAll`, `AdminPaperService.getAllPapers`, `AIAnalysisService.findAll`,
`LeaderboardEntryService.findAll`, `StudentAnswerService.findAll`,
`StudentPaperAttemptService.findAll`, `UserService.getAllUsers`,
`AdminBundleService.getAllBundlesWithStats()` (no-arg). Callers already used paginated variants.

**Regression guard:** `ScalabilityArchitectureTest` (ArchUnit) fails the build if any service
calls a no-arg `findAll()` on a repository, except the allow-list (reference tables +
`AnalyticsService` [N-SC3] + `PaperBundleService` [B3]).

## C. Frontend remediation plan

Goal: backend becomes paginated/secured while the **UI looks identical**. Strategy per call:
1. **Ownership fixes (A1–A3):** no UI change; if a service sent `userId`, keep sending it
   (ignored) or drop it. Verify lists still render.
2. **Pagination (B1–B4):** update the service layer to unwrap `.content`; where the UI showed
   "everything", either (a) request `size=<large cap>` to preserve a single-page feel, or
   (b) add real paging/"load more" where lists can grow without bound (papers, bundles,
   questions). Page metadata wired into existing table pagers where present (admin tables
   already use Ant Design pagination).
3. Centralize Page unwrapping in a tiny helper (e.g. `unwrapPage<T>(res): T[]`) to minimize
   churn and keep types stable.

Detailed per-file frontend caller map is appended in section **D** as each endpoint is changed.

## D. Frontend caller map

| Backend change | Frontend file(s) | Action taken |
|---|---|---|
| A1 `/api/progress` own-data | `services/progressService.ts` → `app/progress/page.tsx` | None — page always passes the logged-in user's id; backend now ignores the param and returns own rows. Behaviour identical. |
| A2 `/api/notifications` own-data | — | No frontend caller. |
| A3 `/api/carts` own-data | `services/cartService.ts` | None — reads `response.data[0]`; still works (returns caller's cart). (Service is legacy/partly mismatched; flagged separately.) |
| A6/B4 `/api/questions` admin+page | — | No frontend caller (`import-from-pdf`, `extract-batch` only). |
| B1 `/api/papers` → Page | `services/paperService.ts` (`getBundlePapers`) | Repointed to `GET /api/paper-bundles/{id}` `.papers`. `getAllPapers()`→`/available` (capped) and `searchPapers()`→`/search` (already Page) unchanged. |
| B2 `/api/admin/bundles` → Page | `services/adminService.ts` (`getBundles`) → `app/admin/bundles/page.tsx`, `app/admin/papers/page.tsx`, `app/admin/users/[id]/page.tsx` | Service requests `size=100`, unwraps `.content`, returns flat array → 3 pages unchanged. |
| B3 public catalog | `services/bundleService.ts` etc. | **Deferred** with backend B3 (storefront UX decision). |

Pre-existing issues surfaced (not regressions from this work, tracked under N-S9 / follow-ups):
`cartService.ts` expects a `bundleIds` JSON field the backend model no longer has; the
entity-body CRUD controllers (Cart/Notification/Progress/QuestionOption/OverallPaperAnalysis)
still allow `GET/PUT/DELETE /{id}` without ownership checks.
