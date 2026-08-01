package com.talentiq.service;

import com.talentiq.dto.request.ProfileUpdateRequest;
import com.talentiq.dto.response.ProfileResponse;

public interface ProfileService {
    ProfileResponse getProfile(Long userId);
    ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request);
}
