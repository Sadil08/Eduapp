package com.eduapp.backend.service;

import com.eduapp.backend.dto.RegisterRequest;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // --- Register normal student ---
    public User register(RegisterRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("RegisterRequest cannot be null");
        }
        userRepository.findByEmail(req.getEmail())
                .ifPresent(u -> { throw new RuntimeException("Email already exists"); });

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setUsername(req.getName());

        // Force all public registrations to student
        if (req.getRole() != null && req.getRole() == Role.ADMIN) {
            throw new RuntimeException("You cannot register as ADMIN");
        }
        user.setRole(Role.STUDENT);

        return userRepository.save(user);
    }

    // --- Create admin manually (restricted) ---
    public User createAdmin(RegisterRequest req) {
        userRepository.findByEmail(req.getEmail())
                .ifPresent(u -> { throw new RuntimeException("Email already exists"); });

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setUsername(req.getName());
        user.setRole(Role.ADMIN);

        return userRepository.save(user);
    }

    // --- Login ---
    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return jwtUtil.generateToken(email,user.getRole(), user.getId());
    }

    // --- Spring Security support ---
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
