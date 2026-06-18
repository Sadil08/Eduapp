-- Enum values were added after Hibernate first generated CHECK constraints pinned to the
-- original values:
--   * users.role           gained SCHOOL_ADMIN, TEACHER, SCHOOL_STUDENT
--   * school_enrolments.status gained PENDING, REJECTED
-- ddl-auto=update does NOT alter an existing column's CHECK constraint, so persistent
-- (non create-drop) databases reject rows with the new values, e.g.:
--   ERROR: new row for relation "users" violates check constraint "users_role_check"
--
-- Drop the auto-generated enum CHECKs. Enum validity is enforced in the application layer
-- (@Enumerated(STRING) + bean validation), and dropping avoids this breaking again whenever a
-- new enum value is added. IF EXISTS keeps it safe across environments / Hibernate versions.
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE school_enrolments DROP CONSTRAINT IF EXISTS school_enrolments_status_check;
