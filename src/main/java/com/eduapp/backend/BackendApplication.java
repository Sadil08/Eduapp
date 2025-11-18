package com.eduapp.backend;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@eduapp.com").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@eduapp.com");
                admin.setPassword(passwordEncoder.encode("12345678"));
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
                System.out.println("✅ Default admin created: admin@eduapp.com / 12345678");
            }
        };
    }
}
