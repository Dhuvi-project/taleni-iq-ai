package com.talentiq.controller;

import com.talentiq.dto.response.UserResponse;
import com.talentiq.entity.enums.Role;
import com.talentiq.exception.GlobalExceptionHandler;
import com.talentiq.security.AppUserPrincipal;
import com.talentiq.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void me_authenticatedPrincipal_returns200WithUser() throws Exception {
        AppUserPrincipal principal = new AppUserPrincipal(1L, UUID.randomUUID(), "jane.doe@example.com", "Jane Doe", Role.CANDIDATE);
        UserResponse response = new UserResponse(1L, "Jane Doe", "jane.doe@example.com", Role.CANDIDATE);
        when(authService.getCurrentUser(1L)).thenReturn(response);

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }
}
