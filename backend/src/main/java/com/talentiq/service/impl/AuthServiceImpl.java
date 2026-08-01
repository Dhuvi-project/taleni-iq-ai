package com.talentiq.service.impl;

import com.talentiq.dto.response.UserResponse;
import com.talentiq.entity.User;
import com.talentiq.exception.NotFoundException;
import com.talentiq.mapper.UserMapper;
import com.talentiq.repository.UserRepository;
import com.talentiq.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }
}
