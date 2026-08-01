package com.talentiq.service;

import com.talentiq.dto.response.UserResponse;
import com.talentiq.entity.User;
import com.talentiq.entity.enums.Role;
import com.talentiq.exception.NotFoundException;
import com.talentiq.mapper.UserMapper;
import com.talentiq.repository.UserRepository;
import com.talentiq.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void getCurrentUser_existingUser_returnsMappedResponse() {
        User user = User.builder()
                .id(2L)
                .authUserId(UUID.randomUUID())
                .name("Existing User")
                .email("existing@example.com")
                .role(Role.CANDIDATE)
                .createdAt(Instant.now())
                .build();
        UserResponse mapped = new UserResponse(2L, "Existing User", "existing@example.com", Role.CANDIDATE);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(mapped);

        UserResponse response = authService.getCurrentUser(2L);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getEmail()).isEqualTo("existing@example.com");
    }

    @Test
    void getCurrentUser_unknownId_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
