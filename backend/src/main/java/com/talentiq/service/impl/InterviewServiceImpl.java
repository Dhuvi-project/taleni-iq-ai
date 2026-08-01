package com.talentiq.service.impl;

import com.talentiq.ai.AiService;
import com.talentiq.dto.request.InterviewGenerateRequest;
import com.talentiq.dto.response.InterviewQuestionItem;
import com.talentiq.dto.response.InterviewResponse;
import com.talentiq.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final AiService aiService;

    @Override
    public InterviewResponse generate(InterviewGenerateRequest request) {
        List<InterviewQuestionItem> questions = aiService.generateInterviewQuestions(request.getRole(), request.getType());
        return new InterviewResponse(request.getRole(), questions);
    }
}
