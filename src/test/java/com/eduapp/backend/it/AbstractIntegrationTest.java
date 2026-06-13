package com.eduapp.backend.it;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Base for full-stack integration tests. Boots the real Spring context (security
 * filter chain, JPA, Redis) against the THROWAWAY {@code eduapp_test} database.
 * The real {@code eduapp_db} is never used by tests.
 *
 * <p>No Docker in this environment, so we target a locally-created scratch DB rather
 * than Testcontainers. The DB name is forced to {@code eduapp_test} here; the username
 * and password are read from the project {@code .env} (gitignored) so no secret is
 * committed and the values always match the local Postgres.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void forceScratchDatabase(DynamicPropertyRegistry registry) {
        Map<String, String> env = readDotEnv();
        // Guarantees no test can accidentally point at the real eduapp_db.
        registry.add("spring.datasource.url",
                () -> "jdbc:postgresql://localhost:5432/eduapp_test");
        registry.add("spring.datasource.username",
                () -> env.getOrDefault("SPRING_DATASOURCE_USERNAME", "postgres"));
        registry.add("spring.datasource.password",
                () -> env.getOrDefault("SPRING_DATASOURCE_PASSWORD", ""));
    }

    private static Map<String, String> readDotEnv() {
        Map<String, String> map = new HashMap<>();
        Path env = Paths.get(".env");
        if (!Files.exists(env)) {
            return map;
        }
        try {
            for (String line : Files.readAllLines(env)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                int eq = line.indexOf('=');
                map.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read .env for integration-test DB credentials", e);
        }
        return map;
    }
}
