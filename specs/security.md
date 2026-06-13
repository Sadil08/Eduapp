# Security specification: backend
**Risk level:** critical  **Score:** 0.15
## Summary
The backend service has multiple critical and high-severity vulnerabilities that would allow unauthenticated or unprivileged actors to access sensitive administrative data and operations. Two admin-path controllers (AdminCustomBundleController, AdminDashboardController) carry no Spring Security authorization annotations, meaning any unauthenticated HTTP caller can reach endpoints that read pending-approval bundle queues and total system revenue. A third controller (AIAnalysisController) is similarly unprotected. A hardcoded PostgreSQL password ('password') is committed inside TestDB.java, and the AdminController endpoint returns raw User entity objects that likely serialize password hashes to callers. These issues collectively represent broken access control (OWASP A01), sensitive data exposure (OWASP A02), and security misconfiguration (OWASP A05) at a severity level that makes the application critically unsafe to expose to the internet.
## Known vulnerabilities and required remediations
### [CRITICAL] auth — src/main/java/com/eduapp/backend/controller/AdminCustomBundleController.java
The controller is mapped to /api/admin/custom-bundles and handles sensitive admin operations (viewing pending-approval bundles, approving/rejecting them), yet there is no @PreAuthorize annotation at the class level or on any visible method, and no @Secured annotation is present. Unlike sibling controllers such as AdminBundleController or AdminFeedbackController that use @PreAuthorize("hasRole('ADMIN')"), this controller is entirely unguarded. Any unauthenticated HTTP client can read, approve, or reject custom bundle submissions, potentially manipulating content availability for all students.

**Fix:** Add @PreAuthorize("hasRole('ADMIN')") at the class level, identical to AdminBundleController:
@RestController
@RequestMapping("/api/admin/custom-bundles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomBundleController { ... }
### [CRITICAL] auth — src/main/java/com/eduapp/backend/controller/AdminDashboardController.java
The /api/admin/dashboard/revenue endpoint exposes aggregated financial revenue data (total platform revenue via BigDecimal) with no authorization annotation whatsoever — no @PreAuthorize, no @Secured, no role-based security at class or method level. Any anonymous caller can issue GET /api/admin/dashboard/revenue and retrieve business-sensitive financial metrics, constituting both a broken access control flaw and a sensitive data exposure.

**Fix:** Add @PreAuthorize("hasRole('ADMIN')") at the class or method level:
@GetMapping("/revenue")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Map<String, BigDecimal>> getTotalRevenue() { ... }
### [HIGH] auth — src/main/java/com/eduapp/backend/controller/AIAnalysisController.java
Neither the class nor the visible handler methods (@GetMapping, @GetMapping("/{id}")) carry any Spring Security authorization annotation. The /api/ai-analyses and /api/ai-analyses/{id} endpoints are therefore accessible without authentication. Depending on what AIAnalysis records contain (student performance data, answer analysis, etc.) this constitutes a broken access control and potential PII exposure flaw. The truncated source prevents confirming whether a later method adds protection, but the class-level annotation is definitively absent.

**Fix:** Determine minimum required role (e.g., ADMIN or authenticated student with ownership check) and apply:
@RestController
@RequestMapping("/api/ai-analyses")
@PreAuthorize("hasRole('ADMIN')")
public class AIAnalysisController { ... }
If students should see only their own analyses, enforce per-resource ownership inside the service layer.
### [HIGH] exposure — TestDB.java:8
Database credentials are hardcoded directly in source code: DriverManager.getConnection("jdbc:postgresql://localhost:5432/eduapp", "postgres", "password"). Even though this is a utility/test class, committing plaintext credentials to the repository exposes them in version control history permanently. If the same credentials are reused in any staging or production environment the database is fully compromised. The username 'postgres' (superuser) combined with a trivially guessable password 'password' makes this especially dangerous.

**Fix:** Delete or gitignore TestDB.java entirely. If a connectivity test is needed, load credentials from environment variables or a local .env that is listed in .gitignore:
String url = System.getenv("DB_URL");
String user = System.getenv("DB_USER");
String pass = System.getenv("DB_PASSWORD");
Conn conn = DriverManager.getConnection(url, user, pass);
Rotate the database password immediately if it was ever pushed to a shared repository.
### [HIGH] exposure — src/main/java/com/eduapp/backend/controller/AdminController.java:18
The createAdmin endpoint returns ResponseEntity<User> — the raw JPA/domain entity — directly to the HTTP caller. Java serialization via Jackson will include every field of the User object unless explicitly excluded with @JsonIgnore or a DTO projection. This almost certainly serializes the BCrypt-hashed password field to the response body, constituting credential exposure even though the hash is not the plaintext password. An attacker who can call this endpoint (or MITM the response) obtains hashes suitable for offline cracking.

**Fix:** Return a DTO (e.g., UserResponseDto) that explicitly excludes the password field:
public ResponseEntity<UserResponseDto> createAdmin(@RequestBody RegisterRequest req) {
    User created = userService.createAdmin(req);
    return ResponseEntity.ok(new UserResponseDto(created.getId(), created.getEmail(), created.getRole()));
}
Alternatively, annotate the password field in User with @JsonIgnore.
### [MEDIUM] config — src/main/java/com/eduapp/backend/config/DatabaseMigrationRunner.java:20-25
DDL ALTER TABLE statements are executed unconditionally every time the application starts via CommandLineRunner. This is not a direct injection risk (statements are hardcoded, not built from user input), but it creates a fragile startup that will emit errors or cause contention in multi-instance deployments, and it bypasses proper schema version management. If an attacker can influence the Spring context or environment configuration to inject additional migration logic, this pattern is also a foothold for persistence.

**Fix:** Replace this ad-hoc migration runner with a proper schema migration tool such as Flyway or Liquibase. If Flyway is used, these statements become versioned migration scripts (V1__fix_column_types.sql) that run exactly once and are recorded in the schema_history table. At minimum, wrap each ALTER in a conditional: ALTER TABLE questions ALTER COLUMN text TYPE TEXT is a no-op if already TEXT, but proper tooling eliminates the risk entirely.
### [MEDIUM] auth — src/main/java/com/eduapp/backend/controller/AIAnalysisController.java (and auth endpoints implied by AuthConfig.java)
No rate-limiting controls are visible on any endpoint in the provided code, including the implied authentication/login endpoint. Spring Boot does not apply rate limiting by default. Absence of rate limiting on login/registration endpoints enables credential stuffing, brute-force, and enumeration attacks. The BCryptPasswordEncoder in AuthConfig.java is a good choice, but without rate limiting an attacker can make unlimited attempts.

**Fix:** Implement rate limiting via Spring's Bucket4j integration, a servlet Filter, or a reverse-proxy (nginx/CloudFront) rate-limiting rule. For the login endpoint specifically:
- Maximum 10 failed attempts per IP per 15 minutes.
- Exponential back-off or account lockout after repeated failures.
- Consider adding CAPTCHA for web-facing login.
### [LOW] exposure — TestDB.java:16
e.printStackTrace() is used in the catch block, which in a servlet/application server context can route stack traces (containing JDBC driver versions, schema names, host details) to logs or in some configurations to HTTP responses. While TestDB.java is a standalone utility, the pattern could be copied into production service code.

**Fix:** Use a proper logger: logger.error("Database connectivity test failed", e); and never propagate raw stack traces to HTTP response bodies. In production Spring Boot, configure server.error.include-stacktrace=never in application.properties.

## Security NFRs (auto-enforced by speckit)
- All endpoints must validate and sanitize input
- No hardcoded credentials — all secrets via environment variables
- Auth required on all non-public endpoints
- Rate limiting on all public-facing endpoints
- All SQL via parameterised queries or ORM — no string interpolation
- Errors must not expose stack traces or internal paths in responses
