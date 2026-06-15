package com.eduapp.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Populates {@link TenantContext} for the current request from the authenticated user's
 * school. Runs AFTER {@code JwtAuthenticationFilter} (which sets the authentication).
 *
 * <p>N-SC1: the schoolId is read straight off the {@link AuthenticatedUser} principal that the
 * auth filter already loaded — no per-request {@code findByEmail}. The principal is rebuilt on
 * every request (the auth filter calls {@code loadUserByUsername} each time), so the tenant is
 * still effectively current per AD-2; only the duplicate query is eliminated. The context is
 * ALWAYS cleared in {@code finally} to prevent the value leaking onto the next request served
 * by the same pooled thread.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && auth.getPrincipal() instanceof AuthenticatedUser principal
                    && principal.getSchoolId() != null) {
                TenantContext.setSchoolId(principal.getSchoolId());
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
