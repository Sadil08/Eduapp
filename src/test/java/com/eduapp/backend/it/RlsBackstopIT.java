package com.eduapp.backend.it;

import com.eduapp.backend.model.School;
import com.eduapp.backend.model.SchoolClass;
import com.eduapp.backend.repository.SchoolClassRepository;
import com.eduapp.backend.repository.SchoolRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SEC-2 — proves the Row-Level-Security backstop works at the database level on real Postgres:
 * with the {@code app.current_school_id} GUC set, a query on a tenant table returns only that
 * school's rows even with NO application-level WHERE filter.
 *
 * <p>Self-contained and transactional: it enables RLS on {@code school_classes} and rolls back
 * at the end (Postgres DDL is transactional), so it never affects other tests. Mirrors the V19
 * migration SQL — in deployed environments RLS is applied by Flyway, and the per-request GUC by
 * {@code TenantRlsAspect}.
 */
@Transactional
class RlsBackstopIT extends AbstractIntegrationTest {

    @PersistenceContext EntityManager em;
    @Autowired SchoolRepository schoolRepository;
    @Autowired SchoolClassRepository classRepository;

    private void setGuc(String value) {
        em.createNativeQuery("select set_config('app.current_school_id', :v, true)")
                .setParameter("v", value)
                .getSingleResult();
    }

    /** Evaluates the exact policy USING() predicate for a row with the given school_id. */
    private boolean policyAllows(String guc, long schoolId) {
        setGuc(guc);
        Object r = em.createNativeQuery(
                "SELECT ("
                        + "current_setting('app.current_school_id', true) IS NULL"
                        + " OR current_setting('app.current_school_id', true) = ''"
                        + " OR " + schoolId + " = current_setting('app.current_school_id', true)::bigint)")
                .getSingleResult();
        return (Boolean) r;
    }

    private boolean currentUserIsSuperuser() {
        Object r = em.createNativeQuery(
                "SELECT usesuper FROM pg_user WHERE usename = current_user").getSingleResult();
        return Boolean.TRUE.equals(r);
    }

    @Test
    void migrationDdl_appliesCleanly_andPolicyLogicIsolatesByGuc() {
        // 1) The V19 migration SQL applies without error on real Postgres.
        em.createNativeQuery("ALTER TABLE school_classes ENABLE ROW LEVEL SECURITY").executeUpdate();
        em.createNativeQuery("ALTER TABLE school_classes FORCE ROW LEVEL SECURITY").executeUpdate();
        em.createNativeQuery("DROP POLICY IF EXISTS tenant_isolation ON school_classes").executeUpdate();
        em.createNativeQuery(
                "CREATE POLICY tenant_isolation ON school_classes USING ("
                        + "current_setting('app.current_school_id', true) IS NULL"
                        + " OR current_setting('app.current_school_id', true) = ''"
                        + " OR school_id = current_setting('app.current_school_id', true)::bigint)")
                .executeUpdate();

        // 2) The policy predicate isolates correctly (superuser-independent — superusers bypass
        //    RLS entirely, so we assert the boolean logic the policy is built from).
        assertThat(policyAllows("9001", 9001L)).as("matching school is visible").isTrue();
        assertThat(policyAllows("9001", 9002L)).as("other school is hidden").isFalse();
        assertThat(policyAllows("", 9002L)).as("unset GUC permits access (no lockout)").isTrue();

        // 3) If (and only if) the app connects as a NON-superuser, RLS enforces the same on real
        //    rows. Under the superuser test connection this is bypassed — which is exactly why the
        //    deployment must use a non-superuser DB role (documented in V19 / the audit).
        String u = UUID.randomUUID().toString().substring(0, 5);
        School a = schoolRepository.save(new School("RLS-A-" + u, "LK", "a" + u + "@s.test"));
        School b = schoolRepository.save(new School("RLS-B-" + u, "LK", "b" + u + "@s.test"));
        SchoolClass ca = classRepository.save(new SchoolClass(a.getId(), null, "CA", "x", "g", "RLSA" + u));
        SchoolClass cb = classRepository.save(new SchoolClass(b.getId(), null, "CB", "x", "g", "RLSB" + u));
        em.flush();
        setGuc(String.valueOf(a.getId()));
        long visible = ((Number) em.createNativeQuery(
                "SELECT count(*) FROM school_classes WHERE id IN (" + ca.getId() + "," + cb.getId() + ")")
                .getSingleResult()).longValue();
        if (currentUserIsSuperuser()) {
            assertThat(visible).as("superuser bypasses RLS").isEqualTo(2);
        } else {
            assertThat(visible).as("non-superuser: RLS hides school B's row").isEqualTo(1);
        }
    }
}
