# EduApp Spec Files Reference

## Created Files
- [`architect.md`](architect.md): Business model, user stories, features, functions/flows, special features.
- [`datamodel.md`](datamodel.md): Entities, schemas, mappings, relationships, DB notes.
- [`AIintegration.md`](AIintegration.md): AI data sending, subject-specific models, industry approaches, integration brainstorm.
- [`constitution.md`](constitution.md): SOLID/clean principles, security, testing, architectural designs.
- [`spec.md`](spec.md): What/why, user stories, app idea for spec-driven development.
- [`plan.md`](plan.md): Phased development, industry styles, consistent snippets.
- [`tasks.md`](tasks.md): Granular tasks with logging/commenting details.

## Key References
- Existing codebase: [`src/main/java/com/eduapp/backend/`](../../../src/main/java/com/eduapp/backend/) - Analyzed for styles.
- POM: [`pom.xml`](../../../pom.xml) - Dependencies (Spring Boot 3.3+, JPA, JWT, MapStruct).
- Config: [`src/main/java/com/eduapp/backend/config/`](../../../src/main/java/com/eduapp/backend/config/) - Security setup.
- Controllers: [`src/main/java/com/eduapp/backend/controller/`](../../../src/main/java/com/eduapp/backend/controller/) - REST endpoints.
- Services: [`src/main/java/com/eduapp/backend/service/`](../../../src/main/java/com/eduapp/backend/service/) - Business logic.
- Repositories: [`src/main/java/com/eduapp/backend/repository/`](../../../src/main/java/com/eduapp/backend/repository/) - Data access.
- Entities: [`src/main/java/com/eduapp/backend/model/`](../../../src/main/java/com/eduapp/backend/model/) - JPA models.
- DTOs: [`src/main/java/com/eduapp/backend/dto/`](../../../src/main/java/com/eduapp/backend/dto/) - Data transfer.
- Mappers: [`src/main/java/com/eduapp/backend/mapper/`](../../../src/main/java/com/eduapp/backend/mapper/) - MapStruct.
- Exceptions: [`src/main/java/com/eduapp/backend/exception/`](../../../src/main/java/com/eduapp/backend/exception/) - Global handler.