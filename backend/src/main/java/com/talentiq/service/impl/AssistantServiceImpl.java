package com.talentiq.service.impl;

import com.talentiq.ai.GroqClient;
import com.talentiq.dto.request.AssistantChatRequest;
import com.talentiq.dto.request.ChatMessageDto;
import com.talentiq.dto.response.AssistantChatResponse;
import com.talentiq.service.AssistantService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssistantServiceImpl implements AssistantService {

    private static final String SYSTEM_INSTRUCTION =
            "You are TalentIQ AI's assistant, an AI recruitment platform assistant that helps with "
                    + "resumes, ATS scoring, job matching, interview prep, skill gaps, and career planning. "
                    + "Answer helpfully and concisely.";

    private final GroqClient groqClient;

    public AssistantServiceImpl(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    @Override
    public AssistantChatResponse chat(AssistantChatRequest request) {
        List<GroqClient.ChatTurn> history = new ArrayList<>();
        if (request.getHistory() != null) {
            for (ChatMessageDto turn : request.getHistory()) {
                history.add(new GroqClient.ChatTurn(turn.getRole(), turn.getText()));
            }
        }
        String reply = groqClient.generateChatReply(SYSTEM_INSTRUCTION, history, request.getMessage());
        return new AssistantChatResponse(reply);
    }
}
