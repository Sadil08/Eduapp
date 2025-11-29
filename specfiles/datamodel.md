# EduApp Data Model

## Entities and Schemas

### User
- id (Long, PK)
- username (String, unique)
- email (String, unique)
- password (String, hashed)
- role (Enum: ADMIN, STUDENT)
- createdAt (Timestamp)
- updatedAt (Timestamp)

### Subject
- id (Long, PK)
- name (String)
- description (String)
- createdBy (Long, FK to User)
- createdAt (Timestamp)

### Lesson
- id (Long, PK)
- name (String)
- description (String)
- subjectId (Long, FK to Subject)
- createdBy (Long, FK to User)
- createdAt (Timestamp)

### PaperBundle
- id (Long, PK)
- name (String)
- description (String)
- price (BigDecimal)
- type (Enum: MCQ, ESSAY, BOTH)
- examType (String) // e.g., "Final Exam"
- subjectId (Long, FK to Subject, nullable)
- lessonId (Long, FK to Lesson, nullable)
- isPastPaper (Boolean)
- createdBy (Long, FK to User)
- createdAt (Timestamp)

### Paper
- id (Long, PK)
- name (String)
- description (String)
- bundleId (Long, FK to PaperBundle)
- type (Enum: MCQ, ESSAY, BOTH)
- maxFreeAttempts (Int, default 2)
- createdBy (Long, FK to User)
- createdAt (Timestamp)

### Question
- id (Long, PK)
- paperId (Long, FK to Paper)
- text (String)
- type (Enum: MCQ, ESSAY)
- correctAnswerText (String, for ESSAY)
- createdBy (Long, FK to User)
- createdAt (Timestamp)

### QuestionOption
- id (Long, PK)
- questionId (Long, FK to Question)
- text (String)
- isCorrect (Boolean)
- orderIndex (Int)

### StudentBundleAccess
- id (Long, PK)
- studentId (Long, FK to User)
- bundleId (Long, FK to PaperBundle)
- purchasedAt (Timestamp)
- paymentId (String, nullable) // for future

### StudentPaperAttempt
- id (Long, PK)
- studentId (Long, FK to User)
- paperId (Long, FK to Paper)
- attemptNumber (Int)
- startedAt (Timestamp)
- completedAt (Timestamp, nullable)
- status (Enum: IN_PROGRESS, COMPLETED)
- timeTakenMinutes (Int, nullable)

### StudentAnswer
- id (Long, PK)
- attemptId (Long, FK to StudentPaperAttempt)
- questionId (Long, FK to Question)
- answerText (String, for ESSAY)
- selectedOptionId (Long, FK to QuestionOption, nullable for ESSAY)
- submittedAt (Timestamp)

### AIAnalysis
- id (Long, PK)
- answerId (Long, FK to StudentAnswer)
- feedback (String) // AI-generated
- marks (Int)
- lessonsToReview (String) // comma-separated
- createdAt (Timestamp)

### OverallPaperAnalysis
- id (Long, PK)
- attemptId (Long, FK to StudentPaperAttempt)
- totalMarks (Int)
- overallFeedback (String)
- lessonsLacking (String)
- createdAt (Timestamp)

### Cart
- id (Long, PK)
- userId (Long, FK to User)
- bundleIds (JSON or separate table for many-to-many)

### Notification
- id (Long, PK)
- userId (Long, FK to User)
- message (String)
- type (Enum: INFO, ALERT)
- isRead (Boolean)
- sentAt (Timestamp)

### Progress
- id (Long, PK)
- userId (Long, FK to User)
- bundleId (Long, FK to PaperBundle)
- paperId (Long, FK to Paper)
- status (Enum: NOT_ATTEMPTED, ATTEMPTED)
- completionPercentage (Float)
- timeSpentMinutes (Int)
- updatedAt (Timestamp)

### LeaderboardEntry
- id (Long, PK)
- userId (Long, FK to User, nullable for anonymous)
- paperId (Long, FK to Paper)
- score (Int)
- subjectId (Long, FK to Subject)
- isAnonymous (Boolean)
- createdAt (Timestamp)

## Mappings and Relationships
- User: One-to-many with StudentBundleAccess, Progress, Notification, Cart, LeaderboardEntry.
- Subject: One-to-many with Lesson, PaperBundle, LeaderboardEntry.
- Lesson: One-to-many with PaperBundle.
- PaperBundle: One-to-many with Paper, StudentBundleAccess, Progress.
- Paper: One-to-many with Question, StudentPaperAttempt, Progress, LeaderboardEntry.
- Question: One-to-many with QuestionOption, StudentAnswer.
- StudentPaperAttempt: One-to-many with StudentAnswer, one-to-one with OverallPaperAnalysis.
- StudentAnswer: One-to-one with AIAnalysis.

## DTOs

### UserResponse (existing)
- id, username, email, role, createdAt

### UserDetailDto (new for admin)
- id, username, email, role, createdAt, updatedAt
- accessedBundles: List<PaperBundleDto>
- attemptedPapers: List<StudentPaperAttemptDto>
- progress: List<ProgressDto>
- scores: List<LeaderboardEntryDto>
- aiFeedbackSummaries: List<AIAnalysisDto> // aggregated

### Other DTOs (existing)
- SubjectDto, LessonDto, PaperBundleDto, PaperDto, QuestionDto, QuestionOptionDto, StudentAnswerDto, StudentPaperAttemptDto, AIAnalysisDto, OverallPaperAnalysisDto, CartDto, NotificationDto, ProgressDto, LeaderboardEntryDto

## Database Notes
- Use PostgreSQL in prod, H2 in dev.
- Indexes on FKs, unique constraints on usernames/emails.
- Enums as strings.
- Timestamps with timezone.