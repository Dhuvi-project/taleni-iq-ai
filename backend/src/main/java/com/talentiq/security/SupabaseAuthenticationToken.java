package com.talentiq.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * The verified-JWT authentication result, exposing an {@link AppUserPrincipal} (the local user
 * record) as the principal rather than the raw {@link Jwt}, so controllers can keep using
 * {@code @AuthenticationPrincipal AppUserPrincipal}.
 */
public class SupabaseAuthenticationToken extends AbstractAuthenticationToken {

    private final AppUserPrincipal principal;
    private final Jwt jwt;

    public SupabaseAuthenticationToken(AppUserPrincipal principal, Jwt jwt,
                                        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
