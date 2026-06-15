package com.eduapp.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.eduapp.backend.model.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // SECURITY: secret is externalised to the JWT_SECRET env var (no hardcoded key).
    // The app fails fast at startup if it is missing or shorter than 32 bytes (HS512 needs >=256 bits).
    private final SecretKey key;

    // N-S6: bind every token to this app. Tokens minted for/by a different service (or with a
    // missing iss/aud) are rejected at parse time, blocking cross-service token confusion.
    private final String issuer;
    private final String audience;

    /** Convenience overload (tests): default issuer/audience. */
    public JwtUtil(String secret) {
        this(secret, "eduapp-backend", "eduapp-app");
    }

    @org.springframework.beans.factory.annotation.Autowired
    public JwtUtil(@Value("${JWT_SECRET:}") String secret,
            @Value("${jwt.issuer:eduapp-backend}") String issuer,
            @Value("${jwt.audience:eduapp-app}") String audience) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set and at least 32 bytes long. Configure it via environment variable.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
    }

    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 10; // 10 hours

    /** Legacy 4-arg overload (test helpers): mints a token at tokenVersion 0. */
    public String generateToken(String email, Role role, Long userId, String referralCode) {
        return generateToken(email, role, userId, referralCode, 0);
    }

    public String generateToken(String email, Role role, Long userId, String referralCode, int tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("id", userId);
        claims.put("referralCode", referralCode);
        claims.put("ver", tokenVersion); // N-S5: token version for revocation
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)                  // N-S6
                .audience().add(audience).and()  // N-S6
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key) // Uses HS512 by default
                .compact();
    }

    /** N-S5: token version embedded in the JWT; the auth filter compares it to the user's current version. */
    public int extractTokenVersion(String token) {
        Integer ver = extractClaim(token, claims -> claims.get("ver", Integer.class));
        return ver == null ? 0 : ver;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("id", Long.class));
    }

    public Role extractRole(String token) {
        String roleString = extractClaim(token, claims -> claims.get("role", String.class));
        return Role.valueOf(roleString);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // N-S6: reject tokens whose iss/aud don't match this service (throws JwtException).
        // requireIssuer handles the (plain-string) issuer; audience is a Set, so check it manually.
        Claims claims = Jwts.parser()
                .verifyWith(key) // This is correct!
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (claims.getAudience() == null || !claims.getAudience().contains(audience)) {
            throw new JwtException("Missing or invalid audience claim");
        }
        return claims;
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}