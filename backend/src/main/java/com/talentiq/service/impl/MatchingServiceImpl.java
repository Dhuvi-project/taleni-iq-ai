package com.talentiq.service.impl;

import com.talentiq.ai.AiService;
import com.talentiq.dto.request.MatchRequest;
import com.talentiq.dto.response.MatchResponse;
import com.talentiq.entity.Resume;
import com.talentiq.exception.NotFoundException;
import com.talentiq.repository.ResumeRepository;
import com.talentiq.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final ResumeRepository resumeRepository;
    private final AiService aiService;

    @Override
    public MatchResponse match(MatchRequest request) {
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new NotFoundException("Resume not found with id " + request.getResumeId()));

        return aiService.matchResumeToJob(resume.getParsedText(), request.getJobDescription());
    }
}
