package com.talentiq.service;

import com.talentiq.dto.request.LoginRequest;
import com.talentiq.dto.response.AuthResponse;
import com.talentiq.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);
}
