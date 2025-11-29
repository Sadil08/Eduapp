# EduApp Implementation Plan

## Phased Development
1. **Phase 1: Core Setup** - User auth, subjects/lessons, basic CRUD.
2. **Phase 2: Content Management** - Bundles, papers, questions.
3. **Phase 3: Assessment & AI** - Attempts, answers, AI integration.
4. **Phase 4: Enhancements** - Progress, leaderboards, payments, notifications.
5. **Phase 5: Admin User Management** - UserController for viewing all users and detailed user profiles with accessed bundles, attempted papers, progress, scores, and AI feedback summaries.

## Industry Standard Styles
- **Coding**: Consistent naming (camelCase), JavaDoc, SLF4J logging, dependency injection.
- **Architecture**: Clean layers, SOLID, RESTful APIs.
- **Security**: JWT, role-based.
- **Testing**: JUnit/Mockito, integration tests.

## Consistent Snippets

### REST Controller
```java
@RestController
@RequestMapping("/api/entities")
public class EntityController {
    private static final Logger logger = LoggerFactory.getLogger(EntityController.class);
    private final EntityService entityService;
    private final EntityMapper entityMapper;

    public EntityController(EntityService entityService, EntityMapper entityMapper) {
        this.entityService = entityService;
        this.entityMapper = entityMapper;
    }

    @GetMapping
    public ResponseEntity<List<EntityDto>> getAll() {
        logger.info("Fetching all entities");
        List<Entity> entities = entityService.findAll();
        List<EntityDto> dtos = entityMapper.toDtoList(entities);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<EntityDto> create(@RequestBody EntityDto dto) {
        logger.info("Creating entity: {}", dto);
        Entity entity = entityMapper.toEntity(dto);
        Entity saved = entityService.save(entity);
        EntityDto savedDto = entityMapper.toDto(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }
}
```

### Service
```java
@Service
public class EntityService {
    private static final Logger logger = LoggerFactory.getLogger(EntityService.class);
    private final EntityRepository entityRepository;

    public EntityService(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    public List<Entity> findAll() {
        logger.info("Finding all entities");
        return entityRepository.findAll();
    }

    public Entity save(Entity entity) {
        logger.info("Saving entity: {}", entity);
        return entityRepository.save(entity);
    }
}
```

### Repository
```java
public interface EntityRepository extends JpaRepository<Entity, Long> {}
```

### Entity
```java
@Entity
@Table(name = "entities")
public class Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Getters/setters, constructors, toString
}
```

### DTO
```java
public class EntityDto {
    private Long id;
    private String name;

    // Getters/setters, constructors
}
```

### Mapper
```java
@Mapper(componentModel = "spring")
public interface EntityMapper {
    EntityDto toDto(Entity entity);
    Entity toEntity(EntityDto dto);
    List<EntityDto> toDtoList(List<Entity> entities);
}
```

### JWT Authentication
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### Exception Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handle(RuntimeException ex) {
        Map<String, Object> error = Map.of("error", ex.getMessage(), "code", 400);
        return ResponseEntity.badRequest().body(error);
    }
}
```

### SLF4J Logging
```java
logger.info("Action performed: {}", param);
logger.error("Error: {}", ex.getMessage(), ex);