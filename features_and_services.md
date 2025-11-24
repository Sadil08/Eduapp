# EduApp Backend API Documentation for Frontend Architecture

## Main Purpose and App Idea

EduApp is a comprehensive web-based education platform focused on paper-based assessments with AI-powered feedback. It targets students preparing for exams and administrators managing educational content. The platform enables students to access paper bundles for exam preparation, with integrated AI-powered feedback for personalized learning. Admins manage content, while students purchase and attempt papers, receiving detailed analyses to improve performance.

The app serves as a digital library for educational papers, combining e-commerce (bundle purchases), assessment (attempts with timers), analytics (progress/leaderboards), and AI (intelligent feedback). It supports public browsing, authenticated access, and admin management, with future expansions like mobile apps and advanced AI models.

Revenue primarily from bundle purchases, with freemium model (2 free attempts per paper). Stakeholders: Students (access bundles, attempt papers, receive AI feedback), Admins (manage content, monitor usage).

## User Stories/Features

### Admin Stories
- As an admin, I want to create/edit/delete subjects and lessons to organize content.
- As an admin, I want to create paper bundles with metadata (exam type, subject, lesson, past paper flag) and pricing.
- As an admin, I want to add papers to bundles with descriptions and attempt limits.
- As an admin, I want to create questions (MCQ/Essay) with correct answers and options.
- As an admin, I want to view student analytics and progress reports.
- As an admin, I want to view all users and their basic details (email, username, role, created date).
- As an admin, I want to view detailed information for a specific user, including accessed paper bundles, attempted papers, progress, scores, and AI feedback summaries.

### Student Stories
- As a student, I want to browse public paper bundles and filter by exam, subject, lesson, or type.
- As a student, I want to view bundle details and add to cart.
- As a student, I want to purchase bundles and access them in my dashboard.
- As a student, I want to attempt papers (up to 2 free, then pay for more).
- As a student, I want to receive detailed AI feedback per question and overall analysis per paper.
- As a student, I want to track my progress and view leaderboards.

### Additional Features
- Progress Tracking: Completion percentage, time spent per bundle/paper. Status per paper (NOT_ATTEMPTED, ATTEMPTED).
- Leaderboards: Aggregate scores, anonymize. Students opt-in to share paper-wise marks publicly; else anonymous to self.
- Notifications: Alerts for new bundles, attempt limits, feedback availability. Email via SendGrid.
- Advanced Search/Filtering: By multiple criteria.
- Versioning: Track changes to papers/bundles.
- Cart/Checkout: Basic e-commerce flow with placeholder payment endpoints for easy integration (e.g., Stripe later).
- Timer: Starts on paper view, stops on submit, displays time taken.
- User Management for Admins: View all users, detailed user profiles with accessed bundles, attempted papers, progress, scores, and AI feedback summaries.

## Inferred Frontend Views/Pages

### Public Pages
- **BundleListPage**: Displays all public paper bundles with filters (exam, subject, lesson, type). Maps to GET /api/paper-bundles. Shows bundle name, description, price, type, examType, isPastPaper, subjectId, lessonId.
- **BundleDetailPage**: Shows detailed bundle info, papers preview. Maps to GET /api/paper-bundles/{id}. Includes papers list with name, description, type, maxFreeAttempts.

### Student Pages
- **LoginPage**: Form for email/password login. Maps to POST /api/auth/login. Returns JWT token.
- **RegisterPage**: Form for registration. Maps to POST /api/auth/register. Returns user basic info.
- **DashboardPage**: Shows purchased bundles, progress, leaderboards. Maps to various endpoints like GET /api/student-bundle-accesses (for bundles), GET /api/progress, GET /api/leaderboard-entries.
- **BundlePapersPage**: Lists papers in a bundle. Maps to GET /api/papers (filtered by bundleId).
- **PaperAttemptPage**: Timed paper attempt, questions sequential. Maps to POST /api/student-paper-attempts (start), POST /api/student-answers (submit answers), triggers AI analysis.
- **PaperResultsPage**: Displays AI feedback per question and overall analysis. Maps to GET /api/ai-analyses, GET /api/overall-paper-analyses.
- **ProgressPage**: Shows completion, scores. Maps to GET /api/progress.
- **LeaderboardPage**: Displays rankings. Maps to GET /api/leaderboard-entries.
- **CartPage**: Shows added bundles, checkout. Maps to GET /api/carts, placeholder payment endpoints.

### Admin Pages
- **AdminDashboardPage**: Overview of users, bundles, analytics.
- **UserListPage**: Lists all users. Maps to GET /api/admin/users. Shows id, username, email, role, createdAt.
- **UserDetailPage**: Detailed user profile. Maps to GET /api/admin/users/{id}. Shows accessedBundles (list of PaperBundleDto), attemptedPapers (list of StudentPaperAttemptDto), progress (list of ProgressDto), scores (list of LeaderboardEntryDto), aiFeedbackSummaries (list of AIAnalysisDto).
- **SubjectManagementPage**: CRUD subjects. Maps to /api/subjects endpoints.
- **LessonManagementPage**: CRUD lessons. Maps to /api/lessons endpoints.
- **BundleManagementPage**: CRUD bundles. Maps to /api/paper-bundles endpoints.
- **PaperManagementPage**: CRUD papers. Maps to /api/papers endpoints.
- **QuestionManagementPage**: CRUD questions/options. Maps to /api/questions, /api/question-options endpoints.

## Comprehensive URL Mappings Table

| Endpoint | HTTP Method | Purpose/Why | Authentication | Request Params/Body | Response JSON (structure + example) | Data Flow |
|----------|-------------|-------------|----------------|---------------------|-------------------------------------|-----------|
| `/api/auth/register` | POST | Register new student user. Forces role to STUDENT, prevents admin registration via public endpoint. | None (public) | Body: RegisterRequest<br>- email: String (unique, validated)<br>- password: String (hashed with BCrypt)<br>- name: String (username)<br>- role: Role (ignored, set to STUDENT) | UserResponse<br>- id: Long (new user ID, from User.id)<br>- name: String (username, from User.username)<br>- email: String (from User.email)<br>- role: Role (always STUDENT)<br>Example: `{"id": 1, "name": "john_doe", "email": "john@example.com", "role": "STUDENT"}` | UserService.register() -> UserRepository.save() -> UserResponse |
| `/api/auth/login` | POST | Authenticate user and return JWT token. Validates email/password, generates token with role/userId. | None (public) | Body: LoginRequest<br>- email: String<br>- password: String | JwtResponse<br>- token: String (JWT with claims: email, role, id, expiration 10h)<br>Example: `{"token": "eyJhbGciOiJIUzUxMiJ9..."}` | UserService.login() -> UserRepository.findByEmail() -> JwtUtil.generateToken() |
| `/api/admin/create` | POST | Create admin user manually (restricted). Allows setting role to ADMIN. | JWT with ROLE_ADMIN | Body: RegisterRequest<br>- email: String<br>- password: String<br>- name: String<br>- role: Role (must be ADMIN) | User<br>- id: Long<br>- username: String<br>- email: String<br>- role: Role<br>- createdAt: Timestamp<br>- updatedAt: Timestamp<br>Example: `{"id": 2, "username": "admin", "email": "admin@example.com", "role": "ADMIN", "createdAt": "2023-01-01T00:00:00", "updatedAt": "2023-01-01T00:00:00"}` | UserService.createAdmin() -> UserRepository.save() |
| `/api/admin/users` | GET | Get all users for admin monitoring. Returns basic user list. | JWT with ROLE_ADMIN | None | List<UserResponse><br>- id: Long (from User.id)<br>- name: String (from User.username)<br>- email: String (from User.email)<br>- role: Role (from User.role)<br>Example: `[{"id": 1, "name": "john_doe", "email": "john@example.com", "role": "STUDENT"}]` | UserService.getAllUsers() -> UserRepository.findAll() -> UserResponse list |
| `/api/admin/users/{id}` | GET | Get detailed user profile including activity. Aggregates bundles, attempts, progress, scores, AI feedback. | JWT with ROLE_ADMIN | Path: id (Long, user ID) | UserDetailDto<br>- id: Long (user ID)<br>- username: String<br>- email: String<br>- role: Role<br>- createdAt: LocalDateTime<br>- updatedAt: LocalDateTime<br>- accessedBundles: List<PaperBundleDto> (bundles user purchased)<br>- attemptedPapers: List<StudentPaperAttemptDto> (papers attempted)<br>- progress: List<ProgressDto> (completion status)<br>- scores: List<LeaderboardEntryDto> (leaderboard entries)<br>- aiFeedbackSummaries: List<AIAnalysisDto> (AI feedbacks)<br>Example: `{"id": 1, "username": "john_doe", "email": "john@example.com", "role": "STUDENT", "createdAt": "2023-01-01T00:00:00", "updatedAt": "2023-01-01T00:00:00", "accessedBundles": [{"id": 1, "name": "Math Bundle", "description": "Math papers", "price": 10.0, "type": "MCQ", "examType": "Final", "subjectId": 1, "lessonId": null, "isPastPaper": true}], "attemptedPapers": [{"id": 1, "studentId": 1, "paperId": 1, "attemptNumber": 1, "status": "COMPLETED", "startedAt": "2023-01-01T10:00:00", "completedAt": "2023-01-01T11:00:00", "timeTakenMinutes": 60}], "progress": [{"id": 1, "userId": 1, "bundleId": 1, "paperId": 1, "status": "ATTEMPTED", "completionPercentage": 100.0, "timeSpentMinutes": 60, "updatedAt": "2023-01-01T11:00:00"}], "scores": [{"id": 1, "userId": 1, "paperId": 1, "score": 85, "subjectId": 1, "isAnonymous": false, "createdAt": "2023-01-01T11:00:00"}], "aiFeedbackSummaries": [{"id": 1, "answerId": 1, "feedback": "Good job", "marks": 8, "lessonsToReview": "Algebra", "createdAt": "2023-01-01T11:00:00"}]}` | UserService.getUserDetails() -> Multiple repositories (StudentBundleAccessRepository, StudentPaperAttemptRepository, ProgressRepository, LeaderboardEntryRepository, AIAnalysisRepository) -> Aggregated UserDetailDto |
| `/api/subjects` | GET | Get all subjects for browsing/filtering. | JWT (any role) | None | List<SubjectDto><br>- id: Long (from Subject.id)<br>- name: String (from Subject.name)<br>- description: String (from Subject.description)<br>Example: `[{"id": 1, "name": "Mathematics", "description": "Math subjects"}]` | SubjectService.findAll() -> SubjectRepository.findAll() -> SubjectMapper.toDtoList() |
| `/api/subjects` | POST | Create new subject. | JWT (any role, but admin intended) | Body: SubjectDto<br>- name: String<br>- description: String | SubjectDto<br>- id: Long (new ID)<br>- name: String<br>- description: String<br>Example: `{"id": 1, "name": "Mathematics", "description": "Math subjects"}` | SubjectService.save() -> SubjectRepository.save() -> SubjectMapper.toDto() |
| `/api/subjects/{id}` | GET | Get subject by ID. | JWT (any role) | Path: id (Long) | SubjectDto<br>- id: Long<br>- name: String<br>- description: String<br>Example: `{"id": 1, "name": "Mathematics", "description": "Math subjects"}` | SubjectService.findById() -> SubjectMapper.toDto() |
| `/api/subjects/{id}` | PUT | Update subject. | JWT (any role) | Path: id (Long)<br>Body: SubjectDto | SubjectDto (updated) | SubjectService.save() -> SubjectMapper.toDto() |
| `/api/subjects/{id}` | DELETE | Delete subject. | JWT (any role) | Path: id (Long) | 204 No Content | SubjectService.deleteById() |
| `/api/lessons` | GET | Get all lessons. | JWT (any role) | None | List<LessonDto><br>- id: Long<br>- name: String<br>- description: String<br>- subjectId: Long (from Lesson.subject.id)<br>Example: `[{"id": 1, "name": "Algebra", "description": "Basic algebra", "subjectId": 1}]` | LessonService.findAll() -> LessonRepository.findAll() -> LessonMapper.toDtoList() |
| `/api/lessons` | POST | Create lesson. Validates subject exists. | JWT (any role) | Body: LessonDto<br>- name: String<br>- description: String<br>- subjectId: Long | LessonDto<br>- id: Long<br>- name: String<br>- description: String<br>- subjectId: Long<br>Example: `{"id": 1, "name": "Algebra", "description": "Basic algebra", "subjectId": 1}` | LessonService.save() -> LessonRepository.save() -> LessonMapper.toDto() |
| `/api/lessons/{id}` | GET | Get lesson by ID. | JWT (any role) | Path: id (Long) | LessonDto | LessonService.findById() -> LessonMapper.toDto() |
| `/api/lessons/{id}` | PUT | Update lesson. | JWT (any role) | Path: id (Long)<br>Body: LessonDto | LessonDto | LessonService.save() -> LessonMapper.toDto() |
| `/api/lessons/{id}` | DELETE | Delete lesson. | JWT (any role) | Path: id (Long) | 204 No Content | LessonService.deleteById() |
| `/api/paper-bundles` | GET | Get all bundles for public browsing. | None (public) | None | List<PaperBundleDto><br>- id: Long<br>- name: String<br>- description: String<br>- price: BigDecimal<br>- type: String (MCQ/ESSAY/BOTH)<br>- examType: String<br>- subjectId: Long (nullable)<br>- lessonId: Long (nullable)<br>- isPastPaper: Boolean<br>Example: `[{"id": 1, "name": "Math Bundle", "description": "Math papers", "price": 10.0, "type": "MCQ", "examType": "Final", "subjectId": 1, "lessonId": null, "isPastPaper": true}]` | PaperBundleService.getAll() -> PaperBundleRepository.findAll() -> PaperBundleMapper.toDtoList() |
| `/api/paper-bundles` | POST | Create bundle. Sets relationships manually. | JWT (any role) | Body: PaperBundleDto | PaperBundleDto | PaperBundleService.createBundle() -> PaperBundleRepository.save() -> PaperBundleMapper.toDto() |
| `/api/papers` | GET | Get all papers. | JWT (any role) | None | List<PaperDto><br>- id: Long<br>- name: String<br>- description: String<br>- bundleId: Long (from Paper.bundle.id)<br>- type: String<br>- maxFreeAttempts: Int<br>Example: `[{"id": 1, "name": "Paper 1", "description": "First paper", "bundleId": 1, "type": "MCQ", "maxFreeAttempts": 2}]` | PaperService.findAll() -> PaperRepository.findAll() -> PaperMapper.toDtoList() |
| `/api/papers` | POST | Create paper. Validates bundle exists. | JWT (any role) | Body: PaperDto | PaperDto | PaperService.save() -> PaperRepository.save() -> PaperMapper.toDto() |
| `/api/papers/{id}` | GET | Get paper by ID. | JWT (any role) | Path: id (Long) | PaperDto | PaperService.findById() -> PaperMapper.toDto() |
| `/api/papers/{id}` | PUT | Update paper. | JWT (any role) | Path: id (Long)<br>Body: PaperDto | PaperDto | PaperService.save() -> PaperMapper.toDto() |
| `/api/papers/{id}` | DELETE | Delete paper. | JWT (any role) | Path: id (Long) | 204 No Content | PaperService.deleteById() |
| `/api/questions` | GET | Get all questions. | JWT (any role) | None | List<QuestionDto><br>- id: Long<br>- text: String<br>- type: String (MCQ/ESSAY)<br>- correctAnswerText: String (for ESSAY)<br>- paperId: Long<br>Example: `[{"id": 1, "text": "What is 2+2?", "type": "MCQ", "correctAnswerText": null, "paperId": 1}]` | QuestionService.findAll() -> QuestionRepository.findAll() -> QuestionMapper.toDtoList() |
| `/api/questions` | POST | Create question. Validates paper exists. | JWT (any role) | Body: QuestionDto | QuestionDto | QuestionService.save() -> QuestionRepository.save() -> QuestionMapper.toDto() |
| `/api/questions/{id}` | GET | Get question by ID. | JWT (any role) | Path: id (Long) | QuestionDto | QuestionService.findById() -> QuestionMapper.toDto() |
| `/api/questions/{id}` | PUT | Update question. | JWT (any role) | Path: id (Long)<br>Body: QuestionDto | QuestionDto | QuestionService.save() -> QuestionMapper.toDto() |
| `/api/questions/{id}` | DELETE | Delete question. | JWT (any role) | Path: id (Long) | 204 No Content | QuestionService.deleteById() |
| `/api/question-options` | GET | Get all options. | JWT (any role) | None | List<QuestionOptionDto><br>- id: Long<br>- text: String<br>- isCorrect: Boolean<br>- orderIndex: Int<br>- questionId: Long<br>Example: `[{"id": 1, "text": "4", "isCorrect": true, "orderIndex": 1, "questionId": 1}]` | QuestionOptionService.findAll() -> QuestionOptionRepository.findAll() -> QuestionOptionMapper.toDtoList() |
| `/api/question-options` | POST | Create option. Validates question exists. | JWT (any role) | Body: QuestionOptionDto | QuestionOptionDto | QuestionOptionService.save() -> QuestionOptionRepository.save() -> QuestionOptionMapper.toDto() |
| `/api/question-options/{id}` | DELETE | Delete option. | JWT (any role) | Path: id (Long) | 204 No Content | QuestionOptionService.deleteById() |
| `/api/student-paper-attempts` | GET | Get all attempts. | JWT (any role) | None | List<StudentPaperAttemptDto><br>- id: Long<br>- studentId: Long<br>- paperId: Long<br>- attemptNumber: Int<br>- status: String (IN_PROGRESS/COMPLETED)<br>- startedAt: LocalDateTime<br>- completedAt: LocalDateTime (nullable)<br>- timeTakenMinutes: Int (nullable)<br>Example: `[{"id": 1, "studentId": 1, "paperId": 1, "attemptNumber": 1, "status": "COMPLETED", "startedAt": "2023-01-01T10:00:00", "completedAt": "2023-01-01T11:00:00", "timeTakenMinutes": 60}]` | StudentPaperAttemptService.findAll() -> StudentPaperAttemptRepository.findAll() -> StudentPaperAttemptMapper.toDtoList() |
| `/api/student-paper-attempts` | POST | Start attempt. Validates user/paper. | JWT (student) | Body: StudentPaperAttemptDto | StudentPaperAttemptDto | StudentPaperAttemptService.save() -> StudentPaperAttemptRepository.save() -> StudentPaperAttemptMapper.toDto() |
| `/api/student-paper-attempts/{id}` | GET | Get attempt by ID. | JWT (any role) | Path: id (Long) | StudentPaperAttemptDto | StudentPaperAttemptService.findById() -> StudentPaperAttemptMapper.toDto() |
| `/api/student-paper-attempts/{id}` | DELETE | Delete attempt. | JWT (any role) | Path: id (Long) | 204 No Content | StudentPaperAttemptService.deleteById() |
| `/api/student-answers` | GET | Get all answers. | JWT (any role) | None | List<StudentAnswerDto><br>- id: Long<br>- attemptId: Long<br>- questionId: Long<br>- answerText: String (for ESSAY)<br>- selectedOptionId: Long (nullable, for MCQ)<br>- submittedAt: LocalDateTime<br>Example: `[{"id": 1, "attemptId": 1, "questionId": 1, "answerText": null, "selectedOptionId": 1, "submittedAt": "2023-01-01T10:30:00"}]` | StudentAnswerService.findAll() -> StudentAnswerRepository.findAll() -> StudentAnswerMapper.toDtoList() |
| `/api/student-answers` | POST | Submit answer. Validates attempt/question. | JWT (student) | Body: StudentAnswerDto | StudentAnswerDto | StudentAnswerService.save() -> StudentAnswerRepository.save() -> StudentAnswerMapper.toDto() |
| `/api/student-answers/{id}` | GET | Get answer by ID. | JWT (any role) | Path: id (Long) | StudentAnswerDto | StudentAnswerService.findById() -> StudentAnswerMapper.toDto() |
| `/api/student-answers/{id}` | DELETE | Delete answer. | JWT (any role) | Path: id (Long) | 204 No Content | StudentAnswerService.deleteById() |
| `/api/ai-analyses` | GET | Get all AI analyses. | JWT (any role) | None | List<AIAnalysis><br>- id: Long<br>- answerId: Long<br>- feedback: String<br>- marks: Int<br>- lessonsToReview: String<br>- createdAt: LocalDateTime<br>Example: `[{"id": 1, "answerId": 1, "feedback": "Correct, but explain reasoning", "marks": 9, "lessonsToReview": "Reasoning", "createdAt": "2023-01-01T11:00:00"}]` | AIAnalysisService.findAll() -> AIAnalysisRepository.findAll() |
| `/api/ai-analyses` | POST | Create AI analysis (usually triggered internally). | JWT (any role) | Body: AIAnalysis | AIAnalysis | AIAnalysisService.save() -> AIAnalysisRepository.save() |
| `/api/overall-paper-analyses` | GET | Get all overall analyses. | JWT (any role) | None | List<OverallPaperAnalysis><br>- id: Long<br>- attemptId: Long<br>- totalMarks: Int<br>- overallFeedback: String<br>- lessonsLacking: String<br>- createdAt: LocalDateTime<br>Example: `[{"id": 1, "attemptId": 1, "totalMarks": 85, "overallFeedback": "Good performance, focus on weak areas", "lessonsLacking": "Geometry", "createdAt": "2023-01-01T11:00:00"}]` | OverallPaperAnalysisService.findAll() -> OverallPaperAnalysisRepository.findAll() |
| `/api/overall-paper-analyses` | POST | Create overall analysis (aggregated). | JWT (any role) | Body: OverallPaperAnalysis | OverallPaperAnalysis | OverallPaperAnalysisService.save() -> OverallPaperAnalysisRepository.save() |
| `/api/progress` | GET | Get all progress. | JWT (any role) | None | List<Progress><br>- id: Long<br>- userId: Long<br>- bundleId: Long<br>- paperId: Long<br>- status: String (NOT_ATTEMPTED/ATTEMPTED)<br>- completionPercentage: Float<br>- timeSpentMinutes: Int<br>- updatedAt: LocalDateTime<br>Example: `[{"id": 1, "userId": 1, "bundleId": 1, "paperId": 1, "status": "ATTEMPTED", "completionPercentage": 100.0, "timeSpentMinutes": 60, "updatedAt": "2023-01-01T11:00:00"}]` | ProgressService.findAll() -> ProgressRepository.findAll() |
| `/api/progress` | POST | Create/update progress. | JWT (any role) | Body: Progress | Progress | ProgressService.save() -> ProgressRepository.save() |
| `/api/leaderboard-entries` | GET | Get all leaderboard entries. | JWT (any role) | None | List<LeaderboardEntry><br>- id: Long<br>- userId: Long (nullable for anonymous)<br>- paperId: Long<br>- score: Int<br>- subjectId: Long<br>- isAnonymous: Boolean<br>- createdAt: LocalDateTime<br>Example: `[{"id": 1, "userId": 1, "paperId": 1, "score": 85, "subjectId": 1, "isAnonymous": false, "createdAt": "2023-01-01T11:00:00"}]` | LeaderboardEntryService.findAll() -> LeaderboardEntryRepository.findAll() |
| `/api/leaderboard-entries` | POST | Create leaderboard entry. | JWT (any role) | Body: LeaderboardEntry | LeaderboardEntry | LeaderboardEntryService.save() -> LeaderboardEntryRepository.save() |
| `/api/carts` | GET | Get all carts. | JWT (any role) | None | List<Cart><br>- id: Long<br>- userId: Long<br>- bundleIds: JSON (list of bundle IDs)<br>Example: `[{"id": 1, "userId": 1, "bundleIds": "[1,2]"}]` | CartService.findAll() -> CartRepository.findAll() |
| `/api/carts` | POST | Create cart. | JWT (any role) | Body: Cart | Cart | CartService.save() -> CartRepository.save() |
| `/api/notifications` | GET | Get all notifications. | JWT (any role) | None | List<Notification><br>- id: Long<br>- userId: Long<br>- message: String<br>- type: String (INFO/ALERT)<br>- isRead: Boolean<br>- sentAt: LocalDateTime<br>Example: `[{"id": 1, "userId": 1, "message": "New bundle available", "type": "INFO", "isRead": false, "sentAt": "2023-01-01T12:00:00"}]` | NotificationService.findAll() -> NotificationRepository.findAll() |
| `/api/notifications` | POST | Create notification. | JWT (any role) | Body: Notification | Notification | NotificationService.save() -> NotificationRepository.save() |
| `/api/student-bundle-accesses` | GET | Get all bundle accesses. | JWT (any role) | None | List<StudentBundleAccess><br>- id: Long<br>- studentId: Long<br>- bundleId: Long<br>- purchasedAt: LocalDateTime<br>- paymentId: String (nullable)<br>Example: `[{"id": 1, "studentId": 1, "bundleId": 1, "purchasedAt": "2023-01-01T09:00:00", "paymentId": null}]` | StudentBundleAccessService.findAll() -> StudentBundleAccessRepository.findAll() |

## DTO Code Snippets

### UserResponse
```java
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    // Getters
}
```

### UserDetailDto
```java
public class UserDetailDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PaperBundleDto> accessedBundles;
    private List<StudentPaperAttemptDto> attemptedPapers;
    private List<ProgressDto> progress;
    private List<LeaderboardEntryDto> scores;
    private List<AIAnalysisDto> aiFeedbackSummaries;
    // Getters/Setters
}
```

### PaperBundleDto
```java
public class PaperBundleDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String type;
    private String examType;
    private Long subjectId;
    private Long lessonId;
    private Boolean isPastPaper;
    // Getters/Setters
}
```

### StudentPaperAttemptDto
```java
public class StudentPaperAttemptDto {
    private Long id;
    private Long studentId;
    private Long paperId;
    private Integer attemptNumber;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeTakenMinutes;
    // Getters/Setters
}
```

### AIAnalysisDto
```java
public class AIAnalysisDto {
    private Long id;
    private Long answerId;
    private String feedback;
    private Integer marks;
    private String lessonsToReview;
    private LocalDateTime createdAt;
    // Getters/Setters
}
```

## Verification and Cross-Reference

All endpoints are derived from controllers, services handle business logic, repositories access data, DTOs shape responses, mappers transform data. Security via JWT, role-based access. No ambiguities noted; all flows align with spec.