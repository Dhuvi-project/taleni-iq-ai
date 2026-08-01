package com.talentiq.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.ai.AiService;
import com.talentiq.ai.SkillCatalog;
import com.talentiq.dto.response.CareerRecommendationResponse;
import com.talentiq.entity.Resume;
import com.talentiq.entity.User;
import com.talentiq.exception.NotFoundException;
import com.talentiq.repository.ResumeRepository;
import com.talentiq.repository.UserRepository;
import com.talentiq.service.CareerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CareerServiceImpl implements CareerService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public List<CareerRecommendationResponse> recommend(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException("Resume not found with id " + resumeId));

        User user = userRepository.findById(resume.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id " + resume.getUserId()));

        Set<String> skills = new LinkedHashSet<>();

        if (user.getSkillsJson() != null && !user.getSkillsJson().isBlank()) {
            try {
                List<String> profileSkills = objectMapper.readValue(user.getSkillsJson(), new TypeReference<>() {
                });
                skills.addAll(profileSkills);
            } catch (Exception ignored) {
                // fall through to resume-derived skills
            }
        }

        skills.addAll(SkillCatalog.extractSkills(resume.getParsedText()));

        return aiService.recommendCareers(resume.getParsedText(), new ArrayList<>(skills));
    }
}
