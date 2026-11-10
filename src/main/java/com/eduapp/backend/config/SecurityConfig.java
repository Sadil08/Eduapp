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

        // SECURITY (WP-A0.4): wildcard origin + allowCredentials is unsafe (and invalid per
        // spec). Allow only the explicitly configured frontend origin(s) from FRONTEND_URL
        // (comma-separated). setAllowedOrigins requires exact origins — no wildcards.
        java.util.List<String> origins = java.util.Arrays.stream(frontendUrl.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
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
                // SEC-TOKEN-1 (C3): defense-in-depth security headers on every API response. The
                // API serves only JSON, so a strict CSP + frame denial cost nothing and shrink the
                // XSS/clickjacking surface. (The frontend Next.js app sets its own CSP — see
                // frontend/next.config.* — which is what actually protects the page where the token
                // lives; these headers harden the API tier too.)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
                        .referrerPolicy(ref -> ref.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31_536_000)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // WP-A1.2: health is public for liveness/readiness probes; metrics &
                        // the Prometheus scrape endpoint are ADMIN-only (or scrape via network policy).
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/me").authenticated() // FE-A1.1: server-validated identity
                        .requestMatchers("/api/auth/logout-all").authenticated() // SEC-TOKEN-1 C4
                        .requestMatchers("/api/auth/**").permitAll() // open endpoints
                        .requestMatchers(HttpMethod.POST, "/api/invites/accept").permitAll() // token-gated teacher onboarding
                        // PayHere server-to-server callback: no JWT, authenticated by its md5sig signature.
                        .requestMatchers(HttpMethod.POST, "/api/payments/payhere/notify").permitAll()
                        .requestMatchers("/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        // WP-A0.3f: /api/files/** permitAll removed — media is now served via
                        // short-lived signed URLs embedded (signed) in authorized DTOs; the
                        // legacy public-redirect FileController was deleted.
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
