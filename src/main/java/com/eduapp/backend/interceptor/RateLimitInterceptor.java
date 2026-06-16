package com.eduapp.backend.interceptor;

import com.eduapp.backend.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Per-caller rate limiting (Redis-backed via {@link RedisRateLimiter}, so limits hold
 * across multiple backend instances). Keyed by authenticated user when a JWT is present,
 * otherwise by client IP. Tiered limits: auth (login/register), extraction (heavy AI),
 * and a general default.
 *
 * <p>Note: AI-service endpoints are not publicly exposed and are reached only through this
 * backend, so rate limiting here also protects the AI tier — the AI service itself does
 * not (and must not) rate-limit by IP, since the backend is its sole client.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    // Requests per 60-second window.
    private static final int LIMIT_AUTH = 10;
    private static final int LIMIT_EXTRACTION = 5;
    private static final int LIMIT_GENERAL = 60;

    @Autowired
    private RedisRateLimiter rateLimiter;

    @Autowired
    private JwtUtil jwtUtil;

    // SEC-8: X-Forwarded-For is client-controlled and can be spoofed to rotate the rate-limit
    // key. Only honour it when explicitly enabled (i.e. the app sits behind a trusted proxy/LB
    // that overwrites it). Default false → use the real socket address.
    @org.springframework.beans.factory.annotation.Value("${app.rate-limit.trust-forwarded-header:false}")
    private boolean trustForwardedHeader;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String caller = resolveCaller(request);
        String uri = request.getRequestURI();

        String tier;
        int limit;
        // SEC-5: sensitive tiers fail CLOSED on a Redis outage (no brute-force / cost-abuse
        // window); the general tier fails OPEN so a cache blip doesn't take the app down.
        boolean failOpen;
        if (uri.startsWith("/api/auth")) {
            tier = "auth";
            limit = LIMIT_AUTH;
            failOpen = false;
        } else if (uri.contains("/extract")) {
            tier = "extraction";
            limit = LIMIT_EXTRACTION;
            failOpen = false;
        } else {
            tier = "general";
            limit = LIMIT_GENERAL;
            failOpen = true;
        }

        RedisRateLimiter.Result result = rateLimiter.tryConsume(tier + ":" + caller, limit, failOpen);

        if (result.allowed()) {
            if (result.remaining() >= 0) {
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(result.remaining()));
            }
            return true;
        }

        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(result.retryAfterSeconds()));
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        logger.warn("Rate limit exceeded for {} on {}", caller, uri);
        return false;
    }

    private String resolveCaller(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String email = jwtUtil.extractEmail(authHeader.substring(7));
                if (email != null) {
                    return "user:" + email;
                }
            } catch (Exception e) {
                logger.debug("Rate-limit token parse failed, falling back to IP: {}", e.getMessage());
            }
        }
        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        // SEC-8: only trust X-Forwarded-For when configured to (behind a trusted proxy).
        if (trustForwardedHeader) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
