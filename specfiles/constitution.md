# EduApp Constitution

## Governing Principles

### SOLID Principles
- **Single Responsibility**: Each class/service has one reason to change (e.g., SubjectService only handles subjects).
- **Open-Closed**: Classes open for extension (e.g., add new question types), closed for modification.
- **Liskov Substitution**: Subtypes replace base types without issues (e.g., enums extendable).
- **Interface Segregation**: Clients depend only on methods they use (e.g., repository interfaces minimal).
- **Dependency Inversion**: High-level modules depend on abstractions (e.g., inject repositories via interfaces).

### Clean Architecture
- **Layers**:
  - **Entities**: Core business objects (User, PaperBundle, etc.).
  - **Use Cases**: Business logic (Services).
  - **Controllers**: API endpoints, handle HTTP.
  - **Repositories**: Data access.
- **Dependency Rule**: Inner layers don't depend on outer; use dependency injection.
- **Boundaries**: DTOs/Mappers separate layers.

### Security Patterns
- **JWT Authentication**: Stateless, filter-based, role claims.
- **Role-Based Access**: @PreAuthorize for admin/student endpoints.
- **CORS**: Configured for frontend URL.
- **Input Validation**: @Valid on DTOs, custom exceptions.
- **Password Hashing**: BCrypt.
- **HTTPS**: Enforce in prod.

### Testing Strategies
- **Unit Tests**: JUnit + Mockito for services, repositories, mappers. Mock dependencies.
- **Integration Tests**: Spring Boot Test for controllers, full context.
- **Test Coverage**: Aim 80%+, focus on business logic.
- **TDD/BDD**: Write tests first for critical features (e.g., AI integration).
- **CI/CD**: Automated tests on commits.

### Architectural Designs
- **RESTful APIs**: Standard HTTP methods, status codes, HATEOAS optional.
- **DTOs**: Separate from entities, MapStruct for conversion.
- **Logging**: SLF4J with levels (INFO for ops, DEBUG for dev).
- **Exception Handling**: Global handler, frontend-friendly error DTOs (e.g., {error: "msg", code: 400}).
- **Naming Conventions**: camelCase, PascalCase for classes, consistent prefixes (e.g., StudentAnswer).
- **Documentation**: JavaDoc on public methods, OpenAPI/Swagger for APIs.
- **Performance**: Pagination, caching (Redis later), async for AI calls.
- **Scalability**: Microservices potential (e.g., separate AI service), DB indexing.

### Industry Standards
- **Spring Boot 3.3+**: Latest features, security patches.
- **JPA/Hibernate**: ORM with PostgreSQL/H2.
- **MapStruct**: Type-safe mapping.
- **Version Control**: Git, semantic versioning.
- **Code Quality**: SonarQube, consistent formatting (e.g., Google Java Style).