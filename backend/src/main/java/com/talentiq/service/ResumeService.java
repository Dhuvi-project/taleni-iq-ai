package com.talentiq.service;

import com.talentiq.dto.response.ResumeAnalysisResponse;
import com.talentiq.dto.response.ResumeSummaryResponse;
import com.talentiq.dto.response.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {
    ResumeUploadResponse upload(MultipartFile file, Long targetUserId, Long requesterId);
    List<ResumeSummaryResponse> listByUser(Long userId);
    ResumeAnalysisResponse getAnalysis(Long resumeId);
}
