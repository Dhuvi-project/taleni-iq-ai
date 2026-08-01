package com.talentiq.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.dto.EducationEntry;
import com.talentiq.dto.ExperienceEntry;
import com.talentiq.dto.request.ProfileUpdateRequest;
import com.talentiq.dto.response.ProfileResponse;
import com.talentiq.entity.User;
import com.talentiq.exception.NotFoundException;
import com.talentiq.repository.UserRepository;
import com.talentiq.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id " + userId));
        return toResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id " + userId));

        if (request.getHeadline() != null) {
            user.setHeadline(request.getHeadline());
        }
        if (request.getExperience() != null) {
            user.setExperienceJson(writeJson(request.getExperience()));
        }
        if (request.getEducation() != null) {
            user.setEducationJson(writeJson(request.getEducation()));
        }
        if (request.getSkills() != null) {
            user.setSkillsJson(writeJson(request.getSkills()));
        }

        user = userRepository.save(user);
        return toResponse(user);
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getHeadline(),
                readJson(user.getExperienceJson(), new TypeReference<List<ExperienceEntry>>() {
                }),
                readJson(user.getEducationJson(), new TypeReference<List<EducationEntry>>() {
                }),
                readJson(user.getSkillsJson(), new TypeReference<List<String>>() {
                })
        );
    }

    private <T> List<T> readJson(String json, TypeReference<List<T>> type) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            return List.of();
        }
    }

    @SneakyThrows
    private String writeJson(List<?> values) {
        return objectMapper.writeValueAsString(values);
    }
}
