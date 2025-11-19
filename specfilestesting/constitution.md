# EduApp Testing Constitution

## Governing Principles for Testing

### SOLID Principles in Testing
- **Single Responsibility**: Each test class/method tests one behavior/aspect.
- **Open-Closed**: Tests extensible for new cases without modifying existing.
- **Liskov Substitution**: Test interfaces allow substitution of implementations.
- **Interface Segregation**: Test utilities focused on specific needs.
- **Dependency Inversion**: Tests depend on abstractions, not concretions.

### Clean Testing Architecture
- **Layers**:
  - **Unit Tests**: Test individual methods/classes in isolation.
  - **Integration Tests**: Test component interactions.
  - **System Tests**: End-to-end workflows.
- **Dependency Rule**: Lower-level tests don't depend on higher-level.
- **Boundaries**: Mocks separate external dependencies.

### Security Testing Patterns
- **Authentication Tests**: JWT token validation, role-based access.
- **Input Validation**: Boundary values, SQL injection prevention.
- **Authorization**: @PreAuthorize enforcement.
- **Data Protection**: Password hashing, no sensitive data in logs.

### Testing Strategies
- **Unit Tests**: JUnit 5 + Mockito for services/repositories/mappers. Mock dependencies.
- **Integration Tests**: Spring Boot Test for controllers/full context.
- **Test Coverage**: Aim 80%+, focus on business logic.
- **TDD/BDD**: Write tests first for critical features (e.g., AI integration).
- **CI/CD**: Automated tests on commits.

### Architectural Designs for Testing
- **Test Pyramid**: Many unit, fewer integration, few system tests.
- **Given-When-Then**: BDD style for readability.
- **Fixture Management**: @BeforeEach for setup, @AfterEach for cleanup.
- **Logging in Tests**: Minimal, focus on assertions.
- **Exception Handling**: Test thrown exceptions.
- **Naming Conventions**: testMethodName_ExpectedBehavior.
- **Documentation**: Comments on complex test logic.
- **Performance**: Fast unit tests, async for slow integrations.

### Industry Standards for Testing
- **JUnit 5**: Latest features, parameterized tests, extensions.
- **Mockito**: Mocking framework for dependencies.
- **AssertJ/Hamcrest**: Fluent assertions.
- **TestContainers**: For DB integration tests.
- **JaCoCo**: Coverage reports.
- **Version Control**: Tests committed with code.
- **Code Quality**: Tests follow same style as production.

### Standard Testing Styles
- **AAA Pattern**: Arrange (setup), Act (execute), Assert (verify).
- **Mocking**: Use Mockito for external deps (repos, services).
- **Edge Cases**: Null inputs, empty lists, max values.
- **Happy Path**: Normal flows.
- **Error Paths**: Exceptions, invalid data.

### Methodologies
- **Unit Testing**: Isolate class/method, mock all deps.
- **Integration Testing**: Test service + repo, real DB if possible.
- **Edge-Case Testing**: Boundary values, error conditions.
- **Regression Testing**: Ensure fixes don't break existing.
- **Exploratory Testing**: Manual checks for UI flows.