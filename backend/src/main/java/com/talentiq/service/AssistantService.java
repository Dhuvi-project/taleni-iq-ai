package com.talentiq.service;

import com.talentiq.dto.request.AssistantChatRequest;
import com.talentiq.dto.response.AssistantChatResponse;

public interface AssistantService {

    /** Sends the user's message (with prior conversation history) to the AI assistant and returns its reply. */
    AssistantChatResponse chat(AssistantChatRequest request);
}
