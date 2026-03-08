package com.eduapp.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationRunner.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("Running database schema migrations for Supabase TEXT columns...");
        try {
            // Apply column type changes to TEXT to prevent VARCHAR(4000) errors
            jdbcTemplate.execute("ALTER TABLE questions ALTER COLUMN text TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE questions ALTER COLUMN image_url TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE questions ALTER COLUMN model_answer_image_url TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE questions ALTER COLUMN extracted_text TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE questions ALTER COLUMN correct_answer_text TYPE TEXT;");

            jdbcTemplate.execute("ALTER TABLE student_answers ALTER COLUMN answer_text TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE student_answers ALTER COLUMN image_url TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE student_answers ALTER COLUMN extracted_text TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE student_answers ALTER COLUMN ai_feedback TYPE TEXT;");

            jdbcTemplate.execute("ALTER TABLE question_model_answers ALTER COLUMN answer_text TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE question_model_answers ALTER COLUMN image_url TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE question_model_answers ALTER COLUMN extracted_text TYPE TEXT;");

            logger.info("Database schema migrations completed successfully.");
        } catch (Exception e) {
            logger.error("Error running database schema migration", e);
        }
    }
}
