package com.eduapp.backend.security;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom {@link UserDetails} principal that carries the identity fields the request pipeline
 * needs — userId, schoolId, role and tokenVersion — alongside the standard username/password.
 *
 * <p>N-SC1: {@code TenantFilter} reads {@code schoolId} straight off this principal instead of
 * issuing a second {@code findByEmail} per request (the authentication filter already loaded the
 * user). N-S5: {@code tokenVersion} lets the auth filter reject JWTs minted before a credential
 * change without a DB round-trip beyond the one that already happens.
 */
public class AuthenticatedUser implements UserDetails {

    private final Long userId;
    private final Long schoolId;
    private final String username;
    private final String password;
    private final Role role;
    private final int tokenVersion;

    public AuthenticatedUser(Long userId, Long schoolId, String username, String password,
            Role role, int tokenVersion) {
        this.userId = userId;
        this.schoolId = schoolId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.tokenVersion = tokenVersion;
    }

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user.getId(), user.getSchoolId(), user.getEmail(),
                user.getPassword(), user.getRole(), user.getTokenVersion());
    }

    public Long getUserId() {
        return userId;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public Role getRole() {
        return role;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
