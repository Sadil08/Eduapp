package com.eduapp.backend.it;

import org.junit.jupiter.api.Test;

/** WP-0.3 — proves the full application context boots against the scratch DB. */
class ContextLoadsIT extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // If the Spring context (security + JPA + Redis) fails to start, this test fails.
    }
}
