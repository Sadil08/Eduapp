package com.eduapp.backend.service;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * WP-3.2 — every Role (incl. the new school roles) maps to a ROLE_&lt;name&gt; authority,
 * so {@code hasRole('TEACHER')} etc. work without any extra wiring.
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsAuthorityTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @ParameterizedTest
    @EnumSource(Role.class)
    void loadUserByUsername_mapsRoleToAuthority(Role role) {
        User u = new User("user@school.test", "hashed", "User");
        u.setRole(role);
        when(userRepository.findByEmail("user@school.test")).thenReturn(Optional.of(u));

        UserDetails details = userService.loadUserByUsername("user@school.test");

        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_" + role.name());
    }
}
