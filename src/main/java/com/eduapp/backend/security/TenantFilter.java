package com.eduapp.backend.security;

import com.eduapp.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Populates {@link TenantContext} for the current request from the authenticated user's
 * school. Runs AFTER {@code JwtAuthenticationFilter} (which sets the authentication).
 *
 * <p>Per AD-2 in IMPLEMENTATION_PLAN.md, the tenant is resolved from the DB (not the JWT)
 * so it is always current. The context is ALWAYS cleared in {@code finally} to prevent
 * the value leaking onto the next request served by the same pooled thread.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public TenantFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails details) {
                userRepository.findByEmail(details.getUsername())
                        .map(u -> u.getSchoolId())
                        .ifPresent(TenantContext::setSchoolId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
