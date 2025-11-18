# EduApp Architecture Blueprint

## Business Model
EduApp is a web-based education platform focused on paper-based assessments with AI-powered feedback. It targets students preparing for exams and administrators managing educational content. Revenue primarily from bundle purchases, with freemium model (2 free attempts per paper).

Key stakeholders:
- Students: Access bundles, attempt papers, receive AI feedback.
- Admins: Manage content, monitor usage.
- Future: Payment gateway integration.

## User Stories

### Admin Stories
- As an admin, I want to create/edit/delete subjects and lessons to organize content.
- As an admin, I want to create paper bundles with metadata (exam type, subject, lesson, past paper flag) and pricing.
- As an admin, I want to add papers to bundles with descriptions and attempt limits.
- As an admin, I want to create questions (MCQ/Essay) with correct answers and options.
- As an admin, I want to view student analytics and progress reports.

### Student Stories
- As a student, I want to browse public paper bundles and filter by exam, subject, lesson, or type.
- As a student, I want to view bundle details and add to cart.
- As a student, I want to purchase bundles and access them in my dashboard.
- As a student, I want to attempt papers (up to 2 free, then pay for more).
- As a student, I want to receive detailed AI feedback per question and overall analysis per paper.
- As a student, I want to track my progress and view leaderboards.

## Additional Features
- Progress Tracking: Completion percentage, time spent per bundle/paper. Status per paper (NOT_ATTEMPTED, ATTEMPTED).
- Leaderboards: Aggregate scores, anonymize. Students opt-in to share paper-wise marks publicly; else anonymous to self. Use REST APIs for ranking.
- Notifications: Alerts for new bundles, attempt limits, feedback availability. Email via SendGrid.
- Advanced Search/Filtering: By multiple criteria.
- Versioning: Track changes to papers/bundles.
- Cart/Checkout: Basic e-commerce flow with placeholder payment endpoints for easy integration (e.g., Stripe later).
- Timer: Starts on paper view, stops on submit, displays time taken.

## App Functions and Flows

### Public Flow
1. User visits /bundles: Lists all bundles with filters (exam, subject, etc.).
2. Clicks bundle: Views details (/bundle/{id}), description, papers preview.
3. Adds to cart (requires login for checkout).

### Student Flow
1. Login/Register: JWT authentication.
2. Dashboard: Shows purchased bundles.
3. Select Bundle: Lists papers (Paper 1, Paper 2, ...).
4. Select Paper: Checks attempts left; if exceeded, prompt payment.
5. Attempt Paper: Timer starts on view. Questions displayed sequentially.
   - MCQ: Options shown, select one.
   - Essay: Text input.
   - Submit answer: No immediate AI; save answer.
6. Complete Paper: On submit, timer stops, display time taken. Send full paper (all questions/answers) to AI for analysis. Aggregate into overall feedback, display results.
7. View Progress: Dashboard shows completion, scores.

### Admin Flow
1. Login: Admin role.
2. Manage Content: CRUD for subjects, lessons, bundles, papers, questions.
3. Analytics: View student usage, scores.

### Special Features
- AI Integration: External service analyzes student answers vs. correct answers + question. Returns feedback (what's wrong, lessons to review), marks (constant for MCQ, variable for essay). For overall: Synthesize per-question feedbacks into paper-level analysis (total marks, lacking knowledge areas).
- Security: JWT for auth, role-based access (public read, student write own, admin full).
- Scalability: JPA with PostgreSQL, H2 for dev.

## Ambiguities
- AI service details: Assume REST API endpoint, input JSON (question, correctAnswer, studentAnswer), output JSON (feedback, marks, lessons).
- Payment: Placeholder endpoints, integrate Stripe/PayPal later.
- Notifications: Email service (e.g., SendGrid).
- Leaderboards: Aggregate scores, anonymize.