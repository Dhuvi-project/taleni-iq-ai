package com.talentiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AssistantChatRequest {

    @NotBlank(message = "message is required")
    private String message;

    /** Prior conversation turns, oldest first. May be null/empty for the first message. */
    private List<ChatMessageDto> history;
}
