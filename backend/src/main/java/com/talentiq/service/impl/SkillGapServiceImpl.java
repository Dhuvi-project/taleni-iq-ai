package com.talentiq.service.impl;

import com.talentiq.ai.AiService;
import com.talentiq.dto.response.SkillGapResponse;
import com.talentiq.entity.Resume;
import com.talentiq.exception.NotFoundException;
import com.talentiq.repository.ResumeRepository;
import com.talentiq.service.SkillGapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkillGapServiceImpl implements SkillGapService {

    private final ResumeRepository resumeRepository;
    private final AiService aiService;

    @Override
    public SkillGapResponse computeGap(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException("Resume not found with id " + resumeId));

        SkillGapResponse response = aiService.computeSkillGap(resume.getParsedText());
        response.setResumeId(resume.getId());
        return response;
    }
}
