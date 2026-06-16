-- SEC-2: Postgres Row-Level Security backstop for multi-tenancy.
--
-- Tenancy is enforced in the application (TenantContext + @TenantScoped + ArchUnit), but a single
-- missing ...BySchoolId filter or a new native query could leak across schools. RLS makes the
-- database itself enforce the boundary as defence-in-depth.
--
-- Mechanism: the backend sets a transaction-local GUC `app.current_school_id` at the start of
-- each tenant operation (see TenantRlsAspect). Policies below restrict rows to that school.
--
-- SAFETY: the policy ALSO permits access when the GUC is unset/empty, so trusted non-tenant code
-- paths (admin/global, migrations, the table owner) keep working and the app cannot be locked out
-- by a missing SET. The attacker-controlled path (a tenant request) always has the GUC set, so the
-- leak vector is covered. The GUC is server-set, never client-supplied.
--
-- NOTE: RLS is bypassed by the table OWNER / superuser. For full enforcement the app should connect
-- as a non-owner role with no BYPASSRLS — track that with the deployment hardening (OPS).

DO $$
DECLARE
    t text;
    tenant_tables text[] := ARRAY[
        'school_classes', 'school_papers', 'school_enrolments',
        'school_invites', 'school_paper_attempts'
    ];
BEGIN
    FOREACH t IN ARRAY tenant_tables LOOP
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', t);
        -- FORCE so the policy applies even when the app connects as the table owner.
        EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', t);
        EXECUTE format('DROP POLICY IF EXISTS tenant_isolation ON %I', t);
        EXECUTE format($p$
            CREATE POLICY tenant_isolation ON %I
            USING (
                current_setting('app.current_school_id', true) IS NULL
                OR current_setting('app.current_school_id', true) = ''
                OR school_id = current_setting('app.current_school_id', true)::bigint
            )
        $p$, t);
    END LOOP;
END $$;
