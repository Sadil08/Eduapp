package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GeoLocationService geoLocationService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest req = new RegisterRequest("test@example.com", "password", "Test User", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        // Mock GeoLocationService
        when(geoLocationService.getCountryFromIp(any())).thenReturn("Test Country");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setUsername("Test User");
        savedUser.setRole(Role.STUDENT);
        savedUser.setCountry("Test Country");
        savedUser.setRegistrationIp("127.0.0.1");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.register(req, "127.0.0.1");

        // Assert
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getRole()).isEqualTo(Role.STUDENT);
        assertThat(result.getCountry()).isEqualTo("Test Country");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        // Arrange
        RegisterRequest req = new RegisterRequest("existing@example.com", "password", "Test User", null);
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThatThrownBy(() -> userService.register(req, "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void login_Success() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        user.setRole(Role.STUDENT);
        user.setId(1L);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com", Role.STUDENT, 1L, null)).thenReturn("jwtToken");

        // Act
        String result = userService.login("test@example.com", "password", "127.0.0.1");

        // Assert
        assertThat(result).isEqualTo("jwtToken");
        // Verify user was saved (login stats update)
        verify(userRepository).save(user);
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.login("test@example.com", "wrong", "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid password");
    }
}