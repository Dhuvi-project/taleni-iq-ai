package com.talentiq.service;

import com.talentiq.dto.response.UserResponse;

public interface AuthService {
    UserResponse getCurrentUser(Long userId);
}
