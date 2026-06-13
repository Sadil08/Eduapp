package com.eduapp.backend.service;

import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * WP-1.1 — proves the privilege-escalation fix: public self-registration must
 * never honour a client-supplied role. A registrant requesting ADMIN is a STUDENT.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceRegistrationTest {

    @Mock UserRepository userRepository;
    @Mock org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock GeoLocationService geoLocationService;

    @InjectMocks UserService userService;

    private void stubHappyPath() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        lenient().when(userRepository.findByReferralCode(anyString())).thenReturn(Optional.empty());
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        lenient().when(geoLocationService.getCountryFromIp(anyString())).thenReturn("LK");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void register_ignoresClientSuppliedAdminRole_andAssignsStudent() {
        stubHappyPath();
        RegisterRequest req = new RegisterRequest("a@b.com", "pw123456", "Alice", Role.ADMIN);

        User saved = userService.register(req, "1.2.3.4");

        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void register_ignoresClientSuppliedSchoolAdminRole_andAssignsStudent() {
        stubHappyPath();
        RegisterRequest req = new RegisterRequest("c@d.com", "pw123456", "Carol", Role.SCHOOL_ADMIN);

        User saved = userService.register(req, "1.2.3.4");

        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void register_withNullRole_assignsStudent() {
        stubHappyPath();
        RegisterRequest req = new RegisterRequest("e@f.com", "pw123456", "Eve", null);

        User saved = userService.register(req, "1.2.3.4");

        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
    }
}
