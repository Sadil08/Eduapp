package com.eduapp.backend.security;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.repository.StudentAnswerRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SEC-IDOR (Sprint-0): proves the object-level authorization guard for consumer attempts/answers.
 * The vulnerability was that any authenticated student could read/modify/delete another student's
 * attempt or answer (or trigger paid AI) by guessing ids. The guard must allow the owner and
 * ADMIN, and deny everyone else with a 404 (no enumeration oracle).
 */
@ExtendWith(MockitoExtension.class)
class AttemptAccessGuardTest {

    @Mock StudentPaperAttemptRepository attemptRepository;
    @Mock StudentAnswerRepository studentAnswerRepository;

    @InjectMocks AttemptAccessGuard guard;

    private AuthenticatedUser principal(Long userId, Role role) {
        return new AuthenticatedUser(userId, null, "u" + userId + "@x.com", "pw", role, 0);
    }

    // ---- attempts ----

    @Test
    void owner_canAccessOwnAttempt() {
        when(attemptRepository.findByIdAndStudentId(10L, 1L))
                .thenReturn(Optional.of(new StudentPaperAttempt()));

        assertThat(guard.ownsAttempt(10L, principal(1L, Role.STUDENT))).isTrue();
        assertThatCode(() -> guard.assertOwnsAttempt(10L, principal(1L, Role.STUDENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void nonOwner_isDeniedWith404_andNoEnumeration() {
        // Attempt 10 belongs to user 1; user 2 asks for it.
        when(attemptRepository.findByIdAndStudentId(10L, 2L)).thenReturn(Optional.empty());

        assertThat(guard.ownsAttempt(10L, principal(2L, Role.STUDENT))).isFalse();
        assertThatThrownBy(() -> guard.assertOwnsAttempt(10L, principal(2L, Role.STUDENT)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void admin_canAccessAnyAttempt_withoutOwnershipQuery() {
        assertThat(guard.ownsAttempt(999L, principal(7L, Role.ADMIN))).isTrue();
        // ADMIN short-circuits — no per-object lookup is issued.
        verify(attemptRepository, never()).findByIdAndStudentId(any(), any());
    }

    @Test
    void nullPrincipalOrId_isDenied() {
        assertThat(guard.ownsAttempt(10L, null)).isFalse();
        assertThat(guard.ownsAttempt(null, principal(1L, Role.STUDENT))).isFalse();
    }

    // ---- answers ----

    @Test
    void owner_canAccessOwnAnswer() {
        when(studentAnswerRepository.findByIdAndAttempt_Student_Id(5L, 1L))
                .thenReturn(Optional.of(new StudentAnswer()));

        assertThat(guard.ownsAnswer(5L, principal(1L, Role.STUDENT))).isTrue();
    }

    @Test
    void nonOwner_cannotAccessAnswer() {
        when(studentAnswerRepository.findByIdAndAttempt_Student_Id(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guard.assertOwnsAnswer(5L, principal(2L, Role.STUDENT)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void admin_canAccessAnyAnswer() {
        lenient().when(studentAnswerRepository.findByIdAndAttempt_Student_Id(eq(5L), any()))
                .thenReturn(Optional.empty());

        assertThat(guard.ownsAnswer(5L, principal(7L, Role.ADMIN))).isTrue();
        verify(studentAnswerRepository, never()).findByIdAndAttempt_Student_Id(any(), any());
    }
}
