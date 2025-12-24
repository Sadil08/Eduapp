package com.eduapp.backend.config;

import com.eduapp.backend.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        System.out.println("SECURITY DEBUG: Resolved FRONTEND_URL is: " + frontendUrl);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        java.util.List<String> origins = new java.util.ArrayList<>();
        if (frontendUrl != null && !frontendUrl.isEmpty()) {
            origins.add(frontendUrl);
            // Also allow variants
            if (frontendUrl.contains("localhost")) {
                origins.add(frontendUrl.replace("localhost", "127.0.0.1"));
            }
        }
        // Safety defaults
        if (origins.isEmpty()) {
            origins.add("http://localhost:3000");
        }

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(
                java.util.List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1 hour cache for preflight

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll() // open endpoints
                        .requestMatchers("/api/files/**").permitAll() // serve uploaded files without auth
                        .requestMatchers(HttpMethod.GET, "/api/paper-bundles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/paper-bundles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lessons/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
