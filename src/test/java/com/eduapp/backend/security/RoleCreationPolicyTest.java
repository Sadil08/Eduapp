package com.eduapp.backend.security;

import com.eduapp.backend.model.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.springframework.security.access.AccessDeniedException;

/** SEC-4 — exhaustive check of the role-creation matrix. */
class RoleCreationPolicyTest {

    @Test
    void admin_canCreateAllRoles() {
        for (Role target : Role.values()) {
            assertThat(RoleCreationPolicy.canCreate(Role.ADMIN, target))
                    .as("ADMIN should create " + target).isTrue();
        }
    }

    @Test
    void schoolAdmin_canCreateOnlyTeacherAndSchoolStudent() {
        assertThat(RoleCreationPolicy.canCreate(Role.SCHOOL_ADMIN, Role.TEACHER)).isTrue();
        assertThat(RoleCreationPolicy.canCreate(Role.SCHOOL_ADMIN, Role.SCHOOL_STUDENT)).isTrue();
        assertThat(RoleCreationPolicy.canCreate(Role.SCHOOL_ADMIN, Role.SCHOOL_ADMIN)).isFalse();
        assertThat(RoleCreationPolicy.canCreate(Role.SCHOOL_ADMIN, Role.ADMIN)).isFalse();
        assertThat(RoleCreationPolicy.canCreate(Role.SCHOOL_ADMIN, Role.STUDENT)).isFalse();
    }

    @Test
    void nonPrivilegedRoles_canCreateNothing() {
        for (Role creator : new Role[]{Role.TEACHER, Role.STUDENT, Role.SCHOOL_STUDENT}) {
            for (Role target : Role.values()) {
                assertThat(RoleCreationPolicy.canCreate(creator, target))
                        .as(creator + " must not create " + target).isFalse();
            }
        }
    }

    @Test
    void nulls_areDenied() {
        assertThat(RoleCreationPolicy.canCreate(null, Role.STUDENT)).isFalse();
        assertThat(RoleCreationPolicy.canCreate(Role.ADMIN, null)).isFalse();
    }

    @Test
    void assertCanCreate_throwsWhenDisallowed() {
        assertThatThrownBy(() -> RoleCreationPolicy.assertCanCreate(Role.SCHOOL_ADMIN, Role.ADMIN))
                .isInstanceOf(AccessDeniedException.class);
        // does not throw when allowed
        RoleCreationPolicy.assertCanCreate(Role.ADMIN, Role.SCHOOL_ADMIN);
    }
}
