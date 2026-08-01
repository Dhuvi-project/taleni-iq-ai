package com.talentiq.service.impl;

import com.talentiq.dto.request.LoginRequest;
import com.talentiq.dto.response.AuthResponse;
import com.talentiq.dto.response.UserResponse;
import com.talentiq.entity.User;
import com.talentiq.exception.NotFoundException;
import com.talentiq.exception.UnauthorizedException;
import com.talentiq.mapper.UserMapper;
import com.talentiq.repository.UserRepository;
import com.talentiq.security.JwtService;
import com.talentiq.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = generateToken(user);
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), token);
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }

    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("name", user.getName());
        return jwtService.generateToken(user.getEmail(), claims);
    }
}
