package com.eduapp.backend.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SEC-RLS-1 (B1): startup self-check of the runtime Postgres role.
 *
 * <p>Row-Level Security is silently INEFFECTIVE if the application connects as a superuser or as a
 * role with {@code BYPASSRLS} (Postgres skips all policies for such roles). The tenant RLS
 * backstop (V19) and the {@link com.eduapp.backend.security.TenantRlsAspect} GUC are then a no-op.
 * This check queries the effective role at boot and logs a loud ERROR if RLS would be bypassed, so
 * a misconfigured deployment is caught immediately rather than leaking cross-tenant data silently.
 *
 * <p>Non-fatal and Postgres-specific: any failure (e.g. a non-Postgres test DB) is swallowed.
 */
@Component
public class DbRoleSecurityCheck implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DbRoleSecurityCheck.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        try {
            Object[] row = (Object[]) entityManager
                    .createNativeQuery("SELECT current_user, rolsuper, rolbypassrls "
                            + "FROM pg_roles WHERE rolname = current_user")
                    .getSingleResult();
            String role = String.valueOf(row[0]);
            boolean superuser = Boolean.TRUE.equals(row[1]);
            boolean bypassRls = Boolean.TRUE.equals(row[2]);

            String warning = evaluate(superuser, bypassRls);
            if (warning != null) {
                logger.error("SEC-RLS-1: DB role '{}' {} — Row-Level Security is NOT enforced for this "
                        + "connection. Use a dedicated non-superuser role without BYPASSRLS in production.",
                        role, warning);
            } else {
                logger.info("SEC-RLS-1: DB role '{}' is non-superuser without BYPASSRLS — RLS is enforceable.", role);
            }
        } catch (Exception e) {
            // Non-Postgres / restricted catalog access / startup race — never fail boot for this.
            logger.debug("SEC-RLS-1: could not verify DB role RLS posture: {}", e.getMessage());
        }
    }

    /**
     * Pure decision helper (unit-testable): returns a human-readable reason RLS would be bypassed,
     * or {@code null} if the role is safe.
     */
    static String evaluate(boolean superuser, boolean bypassRls) {
        if (superuser && bypassRls) {
            return "is a SUPERUSER and has BYPASSRLS";
        }
        if (superuser) {
            return "is a SUPERUSER (superusers bypass RLS)";
        }
        if (bypassRls) {
            return "has the BYPASSRLS attribute";
        }
        return null;
    }
}
