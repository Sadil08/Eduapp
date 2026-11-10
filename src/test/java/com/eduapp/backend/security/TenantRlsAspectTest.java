package com.eduapp.backend.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SEC-RLS-1 (B2): proves the tenant RLS GUC is now FAIL-CLOSED. Previously a failure to set
 * {@code app.current_school_id} was swallowed, so the RLS policy could silently degrade to
 * permissive for a tenant request — the exact leak RLS exists to prevent. A tenant request that
 * cannot establish its boundary must now abort.
 */
@ExtendWith(MockitoExtension.class)
class TenantRlsAspectTest {

    @Mock EntityManager entityManager;
    @Mock Query query;

    private TenantRlsAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new TenantRlsAspect();
        ReflectionTestUtils.setField(aspect, "entityManager", entityManager);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void noTenant_doesNothing() {
        TenantContext.clear(); // global / unauthenticated request
        aspect.applyTenantGuc();
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    @Test
    void tenantPresent_setsGucSuccessfully() {
        TenantContext.setSchoolId(7L);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("v"), eq("7"))).thenReturn(query);
        when(query.getSingleResult()).thenReturn("7");

        assertThatCode(() -> aspect.applyTenantGuc()).doesNotThrowAnyException();
    }

    @Test
    void tenantPresent_gucFailure_failsClosed() {
        TenantContext.setSchoolId(7L);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new RuntimeException("db unavailable"));

        assertThatThrownBy(() -> aspect.applyTenantGuc())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tenant isolation");
    }
}
