package com.eduapp.backend.security;

import com.eduapp.backend.model.Role;
import org.springframework.security.access.AccessDeniedException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * SEC-4 — the single source of truth for which roles a given creator may mint. Every
 * authenticated user-creation path MUST consult this so the privilege hierarchy can't be
 * bypassed or drift apart across flows.
 *
 * <pre>
 *   ADMIN        → ADMIN, SCHOOL_ADMIN, TEACHER, SCHOOL_STUDENT, STUDENT
 *   SCHOOL_ADMIN → TEACHER, SCHOOL_STUDENT   (within its own tenant)
 *   TEACHER / STUDENT / SCHOOL_STUDENT → (nothing)
 * </pre>
 *
 * <p>Public self-registration is a separate, creator-less path that always yields STUDENT and
 * never consults this policy (see {@code UserService.register}).
 */
public final class RoleCreationPolicy {

    private static final Map<Role, Set<Role>> ALLOWED = new EnumMap<>(Role.class);

    static {
        ALLOWED.put(Role.ADMIN, EnumSet.of(
                Role.ADMIN, Role.SCHOOL_ADMIN, Role.TEACHER, Role.SCHOOL_STUDENT, Role.STUDENT));
        ALLOWED.put(Role.SCHOOL_ADMIN, EnumSet.of(Role.TEACHER, Role.SCHOOL_STUDENT));
        ALLOWED.put(Role.TEACHER, EnumSet.noneOf(Role.class));
        ALLOWED.put(Role.STUDENT, EnumSet.noneOf(Role.class));
        ALLOWED.put(Role.SCHOOL_STUDENT, EnumSet.noneOf(Role.class));
    }

    private RoleCreationPolicy() {
    }

    /** @return true if a user with {@code creator} may create a user with role {@code target}. */
    public static boolean canCreate(Role creator, Role target) {
        if (creator == null || target == null) {
            return false;
        }
        return ALLOWED.getOrDefault(creator, EnumSet.noneOf(Role.class)).contains(target);
    }

    /** Enforces {@link #canCreate}; throws {@link AccessDeniedException} when not permitted. */
    public static void assertCanCreate(Role creator, Role target) {
        if (!canCreate(creator, target)) {
            throw new AccessDeniedException(
                    "Role " + creator + " is not permitted to create a user with role " + target);
        }
    }
}
