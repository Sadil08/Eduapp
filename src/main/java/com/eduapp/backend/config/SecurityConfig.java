package com.eduapp.backend.config;

import com.eduapp.backend.security.JwtAuthenticationFilter;
import com.eduapp.backend.security.TenantFilter;

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
    private final TenantFilter tenantFilter;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, TenantFilter tenantFilter) {
        this.jwtFilter = jwtFilter;
        this.tenantFilter = tenantFilter;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow all origins for development to prevent CORS issues
        config.setAllowedOriginPatterns(java.util.List.of("*"));

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
                        .requestMatchers(HttpMethod.POST, "/api/invites/accept").permitAll() // token-gated teacher onboarding
                        .requestMatchers("/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/files/**").permitAll() // serve uploaded files without auth
                        .requestMatchers(HttpMethod.GET, "/api/paper-bundles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/paper-bundles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lessons/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/exam-types").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // School-tier namespaces (PRODUCT_BUSINESS_PLAN.md §9.3). Per-endpoint
                        // rules are refined with @PreAuthorize on the controllers.
                        .requestMatchers("/api/school/**").hasAnyRole("SCHOOL_ADMIN", "TEACHER")
                        .requestMatchers("/api/teacher/**").hasAnyRole("TEACHER", "SCHOOL_ADMIN")
                        .requestMatchers("/api/analytics/global/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // TenantFilter runs AFTER JwtAuthenticationFilter so the authenticated
                // principal is available when resolving the tenant.
                .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
