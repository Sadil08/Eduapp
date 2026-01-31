package com.eduapp.backend.interceptor;

import com.eduapp.backend.config.RateLimitConfig;
import com.eduapp.backend.security.JwtUtil;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String key = resolveBucketKey(request);
        String uri = request.getRequestURI();

        Bucket bucket;

        if (uri.startsWith("/api/auth")) {
            bucket = rateLimitConfig.resolveAuthBucket(key);
        } else if (uri.contains("/extract")) {
            bucket = rateLimitConfig.resolveExtractionBucket(key);
        } else {
            bucket = rateLimitConfig.resolveBucket(key);
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Rate limit exceeded. Try again in " + waitForRefill + " seconds.");
            logger.warn("Rate limit exceeded for key: {} on endpoint: {}", key, uri);
            return false;
        }
    }

    private String resolveBucketKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtUtil.extractEmail(token);
                if (email != null) {
                    return "user:" + email;
                }
            } catch (Exception e) {
                // Token invalid or expired, fall back to IP
                logger.debug("Failed to extract user from token for rate limiting: {}", e.getMessage());
            }
        }
        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
