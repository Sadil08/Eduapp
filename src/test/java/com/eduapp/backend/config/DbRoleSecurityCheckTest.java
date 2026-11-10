package com.eduapp.backend.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SEC-RLS-1 (B1): the startup check must flag DB roles for which Postgres bypasses RLS entirely
 * (superuser or BYPASSRLS), so a misconfigured deployment is caught at boot rather than leaking
 * cross-tenant data silently.
 */
class DbRoleSecurityCheckTest {

    @Test
    void safeRole_returnsNull() {
        assertThat(DbRoleSecurityCheck.evaluate(false, false)).isNull();
    }

    @Test
    void superuser_isFlagged() {
        assertThat(DbRoleSecurityCheck.evaluate(true, false)).contains("SUPERUSER");
    }

    @Test
    void bypassRls_isFlagged() {
        assertThat(DbRoleSecurityCheck.evaluate(false, true)).contains("BYPASSRLS");
    }

    @Test
    void superuserAndBypass_isFlagged() {
        assertThat(DbRoleSecurityCheck.evaluate(true, true))
                .contains("SUPERUSER").contains("BYPASSRLS");
    }
}
