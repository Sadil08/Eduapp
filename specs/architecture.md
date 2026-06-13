```markdown
# backend — Architecture

## Overview
`backend` is the server-side component of an educational application ("eduapp"). It is a Spring Boot Java service that manages educational content — including quiz questions with associated image assets — persisted in PostgreSQL, with binary media stored in Supabase S3-compatible object storage. The application exposes a backend API and supports asynchronous processing and response caching.

## Tech stack
| Layer | Technology |
|-------|-----------|
| Language | Java (primary); Python utility scripts |
| Framework | Spring Boot (`@SpringBootApplication`) |
| Database | PostgreSQL (`jdbc:postgresql`, database `eduapp`) |
| Object Storage | Supabase S3-compatible (`eduapp-images` bucket, accessed via boto3) |
| Async Execution | Spring `@EnableAsync` + `ThreadPoolTaskExecutor` |
| Caching | Spring Cache (`@EnableCaching`) |
| Logging | SLF4J |
| Configuration | `.env` file + OS environment variables |

## Module map
| Module | Responsibility |
|--------|---------------|
| `core` | Minimal shared abstractions or cross-module utilities (2 files — exact scope limited by available code) |
| `src` | Main Spring Boot application: controllers, services, repositories, domain model, and infrastructure configuration (219 files under `com.eduapp.backend`) |

## Architecture principles
- **Standard Spring Boot layering** — the package root `com.eduapp.backend` and the presence of a canonical `BackendApplication` entry-point signal adherence to Spring Boot's convention-over-configuration model.
- **Non-blocking async processing** — `@EnableAsync` is declared both on `BackendApplication` and on a dedicated `AsyncConfig` class, with an explicitly configured `ThreadPoolTaskExecutor`, indicating intentional off-thread work rather than ad-hoc thread spawning.
- **Declared caching** — `@EnableCaching` is activated at application bootstrap, indicating that hot or expensive read paths are wrapped in Spring-managed cache abstractions rather than manual in-process maps.
- **Separation of infrastructure configuration** — cross-cutting concerns (async, caching) live in dedicated `@Configuration` classes (e.g., `AsyncConfig`) rather than inside business logic, keeping service code free of infrastructure concerns.
- **Blob assets kept out of the database** — questions carry only an `image_url` reference in PostgreSQL; the actual binaries are stored in an S3-compatible bucket (`eduapp-images`), following the URL-reference pattern for media.
- **Environment-driven secrets** — all sensitive credentials (DB password, S3 endpoint, access key, secret key) are read from environment variables or a `.env` file; no credentials appear hard-coded in production code paths.
- **Operational scripts outside the main artifact** — administrative and diagnostic tasks (e.g., listing S3 objects, ad-hoc DB probing) are handled by standalone Python / plain-Java scripts (`list_s3.py`, `TestDB.java`) rather than being bundled into the deployable JAR.

## Cross-cutting concerns
- **Logging**: SLF4J is used throughout (confirmed in `AsyncConfig`); logger instances are obtained via `LoggerFactory.getLogger`.
- **Error handling**: Async failures are handled through a custom `AsyncUncaughtExceptionHandler` registered in `AsyncConfig`, preventing silent swallowing of exceptions on background threads. Broader REST-layer error handling strategy is not visible in the provided files.
- **Configuration**: Runtime configuration is managed via environment variables and a `.env` file (observed in `list_s3.py` using `python-dotenv`); Spring Boot's standard `application.properties` / `application.yml` mechanism is the expected vehicle for the Java service.
- **Testing**: `TestDB.java` is an ad-hoc connectivity probe used during development/operations. A formal automated test suite (JUnit/Spring Boot Test) is implied by the 219-file `src` module but is not evidenced in the provided samples.

## Security model
- **Authentication**: Not directly observable in the provided files; inferred to exist within the 219-file `src` module.
- **Authorisation**: Not directly observable in the provided files.
- **Secrets management**: Credentials (database password, Supabase S3 access key/secret) are externalised into environment variables and a `.env` file that is not committed to source. No secrets are present in the application entry-point or configuration classes shown.
- **Input validation**: Not directly observable in the provided files; standard Spring Boot projects typically apply `javax.validation` / `jakarta.validation` annotations on request DTOs.

## Data flow (top level)
```
Client request
      │
      ▼
Spring Boot HTTP layer (controllers in com.eduapp.backend)
      │
      ├──► Service layer (business logic, async tasks via ThreadPoolTaskExecutor)
      │         │
      │         ├──► PostgreSQL  ─── stores structured content
      │         │    (questions, metadata, image_url references)
      │         │
      │         └──► Supabase S3 (eduapp-images bucket)
      │              stores image binaries; URL reference returned to client
      │
      └──► Spring Cache (short-circuits repeat reads before hitting the DB)
```
Incoming HTTP requests are dispatched by Spring MVC controllers to the service layer. Read-heavy or expensive operations are intercepted by the Spring cache abstraction before reaching the database. Long-running or I/O-bound work is delegated to the managed async executor. Image binaries are stored in and served from Supabase S3; the database holds only the corresponding URL.
```