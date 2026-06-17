package com.eduapp.backend.config;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** BT-2 — first-admin bootstrap is env-gated and idempotent. */
class FirstAdminBootstrapTest {

    private FirstAdminBootstrap newRunner(String email, UserRepository repo, UserService svc) {
        FirstAdminBootstrap r = new FirstAdminBootstrap(repo, svc);
        ReflectionTestUtils.setField(r, "bootstrapAdminEmail", email);
        return r;
    }

    @Test
    void createsAdmin_whenNoneExistsAndEmailSet() {
        UserRepository repo = mock(UserRepository.class);
        UserService svc = mock(UserService.class);
        when(repo.existsByRole(Role.ADMIN)).thenReturn(false);

        newRunner("boss@eduapp.test", repo, svc).run(null);

        verify(svc).provisionUser(eq("boss@eduapp.test"), any(), eq(Role.ADMIN), isNull());
    }

    @Test
    void doesNothing_whenAdminAlreadyExists() {
        UserRepository repo = mock(UserRepository.class);
        UserService svc = mock(UserService.class);
        when(repo.existsByRole(Role.ADMIN)).thenReturn(true);

        newRunner("boss@eduapp.test", repo, svc).run(null);

        verify(svc, never()).provisionUser(any(), any(), any(), any());
    }

    @Test
    void doesNothing_whenEmailNotConfigured() {
        UserRepository repo = mock(UserRepository.class);
        UserService svc = mock(UserService.class);

        newRunner("", repo, svc).run(null);

        verifyNoInteractions(svc);
    }
}
