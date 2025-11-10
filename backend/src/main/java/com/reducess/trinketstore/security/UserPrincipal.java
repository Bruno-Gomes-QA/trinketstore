package com.reducess.trinketstore.security;

import com.reducess.trinketstore.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID authId;
    private final String email;
    private final String role;
    private final Long userId;

    public UserPrincipal(UUID authId, String email, String role, Long userId) {
        this.authId = authId;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }

    public static UserPrincipal create(User user, String email) {
        return new UserPrincipal(
                user.getAuthId(),
                email,
                user.getRole(),
                user.getIdUser()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

