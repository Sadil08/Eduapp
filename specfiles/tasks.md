# EduApp Implementation Tasks

## Phase 1: Core Setup
1. **Update User Entity**: Add updatedAt, ensure enums. Log: INFO on save. Comment: JavaDoc for fields.
2. **Create Subject/Lesson Entities**: Match datamodel, add FKs. Log: INFO on CRUD. Comment: Business purpose.
3. **Implement Auth Controllers/Services**: JWT login/register. Log: ERROR on failures. Comment: Security notes.
4. **Add Global Exception Handler**: Frontend-friendly errors. Log: WARN on exceptions. Comment: Error codes.

## Phase 2: Content Management
5. **Create PaperBundle/Paper/Question Entities**: With enums, FKs. Log: INFO on creation. Comment: Relationships.
6. **Implement CRUD Controllers/Services**: For bundles/papers. Log: DEBUG for queries. Comment: REST conventions.
7. **Add Mappers/DTOs**: MapStruct for conversions. Log: None. Comment: Mapping rules.
8. **Update Repositories**: Custom queries if needed. Log: None. Comment: Query purposes.

## Phase 3: Assessment & AI
9. **Create Attempt/Answer Entities**: With timeTaken, status. Log: INFO on submit. Comment: Attempt logic.
10. **Implement Attempt Flow**: Timer, save answers. Log: ERROR on AI call fail. Comment: Async notes.
11. **Integrate AI Service**: REST call to external API. Log: INFO on response. Comment: Payload format.
12. **Create Overall Analysis**: Aggregate feedbacks. Log: INFO on completion. Comment: Synthesis logic.

## Phase 4: Enhancements
13. **Add Progress/Leaderboard Entities**: With enums. Log: INFO on updates. Comment: Tracking purpose.
14. **Implement Progress Tracking**: Update on attempts. Log: DEBUG for calculations. Comment: Percentage logic.
15. **Create Leaderboard/Cart Controllers**: Opt-in logic. Log: INFO on shares. Comment: Privacy.
16. **Add Payment Placeholders**: Endpoints for checkout. Log: INFO on placeholders. Comment: Integration points.
17. **Implement Notifications**: Email service. Log: ERROR on send fail. Comment: Templates.

## General Tasks
- **Logging**: Use SLF4J: INFO for ops, ERROR for exceptions, DEBUG for dev. Format: "Action: {}", param.
- **Commenting**: JavaDoc on public methods/classes: @param, @return, @throws. Inline for complex logic.
- **Testing**: Unit tests for services, integration for controllers. Cover 80%.
- **Security**: @PreAuthorize on admin endpoints. Validate inputs.