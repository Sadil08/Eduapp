package com.eduapp.backend.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SEC-2 — sets the transaction-local Postgres GUC {@code app.current_school_id} from
 * {@link TenantContext} before any {@code @TenantScoped} repository call, so the database's
 * row-level-security policies (V19 migration) enforce the tenant boundary as a backstop.
 *
 * <p>Runs as a {@code @Before} on tenant repository methods, so it executes on the same
 * connection/transaction as the query that follows. Uses {@code set_config(..., true)} (a
 * parameterised, transaction-local set) so there is no leakage across pooled connections and no
 * SQL-injection surface. Failures are swallowed: RLS is defence-in-depth and must never break a
 * request — if the GUC can't be set, the policy simply permits access (same as today).
 */
@Aspect
@Component
public class TenantRlsAspect {

    private static final Logger logger = LoggerFactory.getLogger(TenantRlsAspect.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Before("@within(com.eduapp.backend.repository.TenantScoped)")
    public void applyTenantGuc() {
        Long schoolId = TenantContext.getSchoolId();
        if (schoolId == null) {
            return;
        }
        try {
            entityManager
                    .createNativeQuery("select set_config('app.current_school_id', :v, true)")
                    .setParameter("v", String.valueOf(schoolId))
                    .getSingleResult();
        } catch (Exception e) {
            // Backstop only — never fail the request because the GUC couldn't be set.
            logger.debug("Could not set app.current_school_id GUC for RLS: {}", e.getMessage());
        }
    }
}
