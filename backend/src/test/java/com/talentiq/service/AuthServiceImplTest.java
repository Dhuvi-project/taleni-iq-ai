package com.talentiq.service;

import com.talentiq.dto.request.LoginRequest;
import com.talentiq.dto.response.AuthResponse;
import com.talentiq.entity.User;
import com.talentiq.entity.enums.Role;
import com.talentiq.exception.UnauthorizedException;
import com.talentiq.mapper.UserMapper;
import com.talentiq.repository.UserRepository;
import com.talentiq.security.JwtService;
import com.talentiq.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_validCredentials_returnsToken() {
        User user = User.builder()
                .id(2L)
                .name("Existing User")
                .email("existing@example.com")
                .passwordHash("hashed-password")
                .role(Role.CANDIDATE)
                .createdAt(Instant.now())
                .build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("existing@example.com");
        loginRequest.setPassword("CorrectPassword1");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CorrectPassword1", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(anyString(), any())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
    }

    @Test
    void login_wrongPassword_throwsUnauthorizedException() {
        User user = User.builder()
                .id(2L)
                .name("Existing User")
                .email("existing@example.com")
                .passwordHash("hashed-password")
                .role(Role.CANDIDATE)
                .createdAt(Instant.now())
                .build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("existing@example.com");
        loginRequest.setPassword("WrongPassword");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generateToken(anyString(), any());
    }

    @Test
    void login_unknownEmail_throwsUnauthorizedException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nobody@example.com");
        loginRequest.setPassword("whatever");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class);
    }
}
