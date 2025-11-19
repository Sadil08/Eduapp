# EduApp Testing Implementation Plan

## Phased Testing Development
1. **Phase 1: Core Setup** - Auth, subjects/lessons, basic CRUD.
2. **Phase 2: Content Management** - Bundles, papers, questions.
3. **Phase 3: Assessment & AI** - Attempts, answers, AI integration.
4. **Phase 4: Enhancements** - Progress, leaderboards, payments, notifications.

## Industry Standard Styles for Testing
- **Coding**: Consistent naming (testMethod), JavaDoc, SLF4J in tests.
- **Architecture**: Test pyramid, AAA pattern.
- **Security**: Test auth failures, input validation.
- **Testing**: JUnit/Mockito, integration with Spring Test.

## Consistent Snippets for Testing

### Unit Test Service
```java
@SpringBootTest
public class SubjectServiceTest {
    @Mock
    private SubjectRepository repo;
    @InjectMocks
    private SubjectService service;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        when(repo.findAll()).thenReturn(List.of(new Subject("Math")));
        // Act
        List<Subject> result = service.findAll();
        // Assert
        assertThat(result).hasSize(1);
    }
}
```

### Integration Test Controller
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SubjectControllerTest {
    @Autowired
    private TestRestTemplate rest;

    @Test
    void getAll_ReturnsOk() {
        ResponseEntity<List> response = rest.getForEntity("/api/subjects", List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

### Edge-Case Test
```java
@Test
void save_NullSubject_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> service.save(null));
}
```

## Phased Testing Details

### Phase 1: Core Setup
- **Services**: UserService (register/login), SubjectService (CRUD), LessonService (CRUD).
- **Controllers**: AuthController (POST /login), SubjectController (GET/POST /subjects).
- **Features**: Auth with JWT, basic CRUD with validation.
- **How to Test**:
  - Unit: Mock repos, test logic (e.g., password encode, role check).
  - Integration: Test endpoints with real DB, check responses.
  - Happy: Valid input returns entity.
  - Error: Invalid email throws exception, null input fails.
- **Expected Outcomes**: 100% coverage on core, auth works, CRUD succeeds.

### Phase 2: Content Management
- **Services**: PaperBundleService (create/get), PaperService (CRUD), QuestionService (CRUD).
- **Controllers**: PaperBundleController (POST /bundles), PaperController (GET /papers).
- **Features**: Bundle creation with relations, paper/question management.
- **How to Test**:
  - Unit: Mock repos/mappers, test validation (e.g., subject exists).
  - Integration: Full context, test DTO mapping.
  - Happy: Valid bundle created, relations set.
  - Error: Non-existent subject throws error.
- **Expected Outcomes**: Content CRUD reliable, relations intact.

### Phase 3: Assessment & AI
- **Services**: StudentPaperAttemptService (start/submit), AIAnalysisService (analyze).
- **Controllers**: StudentPaperAttemptController (POST /attempts), AIAnalysisController (GET /analyses).
- **Features**: Attempt flow, AI call, feedback storage.
- **How to Test**:
  - Unit: Mock AI service, test attempt logic.
  - Integration: Test submit triggers AI, stores results.
  - Happy: Submit completes, feedback generated.
  - Error: AI failure defaults to basic feedback.
- **Expected Outcomes**: Assessment flows work, AI integrated.

### Phase 4: Enhancements
- **Services**: ProgressService (update), LeaderboardService (rank), CartService (add).
- **Controllers**: ProgressController (PUT /progress), LeaderboardController (GET /leaderboard).
- **Features**: Tracking, rankings, cart/purchase.
- **How to Test**:
  - Unit: Mock repos, test calculations (e.g., percentage).
  - Integration: Test opt-in logic, notifications.
  - Happy: Progress updates, leaderboard shows scores.
  - Error: Anonymous entries handled.
- **Expected Outcomes**: Full features tested, app complete.

## General Testing Tasks
- **Logging**: Test logs errors on failures.
- **Commenting**: JavaDoc on test methods.
- **Coverage**: 80% overall.
- **Security**: Test unauthorized access blocked.