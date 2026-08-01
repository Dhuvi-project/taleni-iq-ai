package com.talentiq.service;

import com.talentiq.dto.request.InterviewGenerateRequest;
import com.talentiq.dto.response.InterviewResponse;

public interface InterviewService {
    InterviewResponse generate(InterviewGenerateRequest request);
}
