package com.eduapp.backend.service;

import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

/**
 * SEC-TOKEN-1 (C4): "log out everywhere" must bump the user's token version so every previously
 * issued JWT is rejected (the mitigation for a token suspected stolen from localStorage). Built
 * with a partial real UserService so we exercise only the new method without its full constructor.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceLogoutAllTest {

    @Mock UserRepository userRepository;

    private UserService newService() {
        // Only userRepository is needed for logoutAllSessions; the rest are null for this path.
        return new UserService(userRepository, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }

    @Test
    void logoutAll_bumpsTokenVersionAndSaves() {
        User user = new User();
        user.setId(42L);
        user.setTokenVersion(3);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        newService().logoutAllSessions(42L);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getTokenVersion()).isEqualTo(4);
    }

    @Test
    void logoutAll_unknownUser_throws() {
        lenient().when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> newService().logoutAllSessions(99L))
                .isInstanceOf(RuntimeException.class);
    }
}
