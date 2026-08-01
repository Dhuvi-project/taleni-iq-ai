package com.talentiq.security;

import com.talentiq.entity.User;
import com.talentiq.entity.enums.Role;
import com.talentiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Converts a verified Supabase Auth JWT into a {@link SupabaseAuthenticationToken}. On a user's
 * first-ever authenticated request, auto-provisions a local {@code User} row keyed by the JWT's
 * {@code sub} claim (the Supabase Auth user id) — there is no separate registration step since
 * Supabase Auth owns the identity/credentials entirely.
 */
@Component
@RequiredArgsConstructor
public class SupabaseJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID authUserId = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");

        User user = userRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> provisionUser(authUserId, email));

        AppUserPrincipal principal = new AppUserPrincipal(
                user.getId(), user.getAuthUserId(), user.getEmail(), user.getName(), user.getRole());

        return new SupabaseAuthenticationToken(principal, jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    private User provisionUser(UUID authUserId, String email) {
        User newUser = User.builder()
                .authUserId(authUserId)
                .email(email)
                .name(deriveName(email))
                .role(Role.CANDIDATE)
                .build();
        return userRepository.save(newUser);
    }

    private String deriveName(String email) {
        if (email == null || email.isBlank()) {
            return "User";
        }
        String local = email.split("@")[0];
        return Character.toUpperCase(local.charAt(0)) + local.substring(1);
    }
}
