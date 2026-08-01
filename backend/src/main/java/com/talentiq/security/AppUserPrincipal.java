package com.talentiq.security;

import com.talentiq.entity.enums.Role;
import lombok.Getter;

import java.util.UUID;

/**
 * The authenticated principal for a request, derived from a verified Supabase Auth JWT and the
 * corresponding local {@code User} row (auto-provisioned on first sign-in — see
 * {@link SupabaseJwtAuthConverter}).
 */
@Getter
public class AppUserPrincipal {

    private final Long userId;
    private final UUID authUserId;
    private final String email;
    private final String name;
    private final Role role;

    public AppUserPrincipal(Long userId, UUID authUserId, String email, String name, Role role) {
        this.userId = userId;
        this.authUserId = authUserId;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
