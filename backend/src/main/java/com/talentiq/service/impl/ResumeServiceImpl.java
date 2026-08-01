package com.talentiq.service.impl;

import com.talentiq.ai.AiService;
import com.talentiq.dto.response.ResumeAnalysisResponse;
import com.talentiq.dto.response.ResumeSummaryResponse;
import com.talentiq.dto.response.ResumeUploadResponse;
import com.talentiq.entity.Resume;
import com.talentiq.exception.NotFoundException;
import com.talentiq.mapper.ResumeMapper;
import com.talentiq.repository.ResumeRepository;
import com.talentiq.service.FileStorageService;
import com.talentiq.service.ResumeParserService;
import com.talentiq.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeParserService resumeParserService;
    private final FileStorageService fileStorageService;
    private final AiService aiService;
    private final ResumeMapper resumeMapper;

    @Override
    @Transactional
    public ResumeUploadResponse upload(MultipartFile file, Long targetUserId, Long requesterId) {
        Long userId = targetUserId != null ? targetUserId : requesterId;

        String parsedText = resumeParserService.extractText(file);
        int nextVersion = (int) resumeRepository.countByUserId(userId) + 1;
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume-" + nextVersion;
        String storagePath = fileStorageService.store(file, userId, "v" + nextVersion + "-" + fileName);

        double atsScore = aiService.analyzeResume(parsedText).getAtsScore();

        Resume resume = Resume.builder()
                .userId(userId)
                .fileName(fileName)
                .version(nextVersion)
                .storagePath(storagePath)
                .parsedText(parsedText)
                .atsScore(atsScore)
                .build();
        resume = resumeRepository.save(resume);

        return resumeMapper.toUploadResponse(resume);
    }

    @Override
    public List<ResumeSummaryResponse> listByUser(Long userId) {
        return resumeRepository.findByUserIdOrderByVersionDesc(userId).stream()
                .map(resumeMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public ResumeAnalysisResponse getAnalysis(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException("Resume not found with id " + resumeId));

        ResumeAnalysisResponse analysis = aiService.analyzeResume(resume.getParsedText());
        analysis.setResumeId(resume.getId());
        return analysis;
    }
}
