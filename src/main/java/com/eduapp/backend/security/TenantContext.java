package com.eduapp.backend.security;

/**
 * Holds the current request's tenant ({@code school_id}) on a ThreadLocal so that
 * tenant scoping is available to the service/repository layer without threading the
 * id through every method signature.
 *
 * <p>MUST be cleared at the end of every request (see {@code TenantFilter}) — the web
 * container reuses threads from a pool, so a leaked value would bleed across tenants.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_SCHOOL_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setSchoolId(Long schoolId) {
        CURRENT_SCHOOL_ID.set(schoolId);
    }

    /** @return the current tenant id, or {@code null} for global-tier / unauthenticated requests. */
    public static Long getSchoolId() {
        return CURRENT_SCHOOL_ID.get();
    }

    public static boolean hasTenant() {
        return CURRENT_SCHOOL_ID.get() != null;
    }

    public static void clear() {
        CURRENT_SCHOOL_ID.remove();
    }
}
