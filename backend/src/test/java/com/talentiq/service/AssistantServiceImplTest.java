package com.talentiq.service;

import com.talentiq.ai.GroqClient;
import com.talentiq.dto.request.AssistantChatRequest;
import com.talentiq.dto.request.ChatMessageDto;
import com.talentiq.dto.response.AssistantChatResponse;
import com.talentiq.service.impl.AssistantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifies the assistant chat service builds the correct Groq call and passes through the
 * reply. {@link GroqClient} is mocked so this never makes a real network call.
 */
@ExtendWith(MockitoExtension.class)
class AssistantServiceImplTest {

    @Mock
    private GroqClient groqClient;

    @Test
    void chat_withNoHistory_callsGroqWithEmptyHistoryAndReturnsReply() {
        AssistantServiceImpl service = new AssistantServiceImpl(groqClient);

        when(groqClient.generateChatReply(anyString(), any(), anyString()))
                .thenReturn("Sure, I can help you improve your resume's ATS score.");

        AssistantChatRequest request = new AssistantChatRequest();
        request.setMessage("How can I improve my ATS score?");

        AssistantChatResponse response = service.chat(request);

        assertThat(response.getReply()).isEqualTo("Sure, I can help you improve your resume's ATS score.");
    }

    @Test
    void chat_withHistory_mapsHistoryTurnsAndSystemInstruction() {
        AssistantServiceImpl service = new AssistantServiceImpl(groqClient);

        when(groqClient.generateChatReply(anyString(), any(), anyString()))
                .thenReturn("Here are three interview tips.");

        AssistantChatRequest request = new AssistantChatRequest();
        request.setMessage("Give me interview tips.");
        request.setHistory(List.of(
                new ChatMessageDto("user", "Hi"),
                new ChatMessageDto("assistant", "Hello! How can I help with your job search?")));

        AssistantChatResponse response = service.chat(request);

        assertThat(response.getReply()).isEqualTo("Here are three interview tips.");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GroqClient.ChatTurn>> historyCaptor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(groqClient).generateChatReply(
                org.mockito.ArgumentMatchers.contains("TalentIQ AI's assistant"),
                historyCaptor.capture(),
                eq("Give me interview tips."));

        List<GroqClient.ChatTurn> capturedHistory = historyCaptor.getValue();
        assertThat(capturedHistory).hasSize(2);
        assertThat(capturedHistory.get(0).role()).isEqualTo("user");
        assertThat(capturedHistory.get(0).text()).isEqualTo("Hi");
        assertThat(capturedHistory.get(1).role()).isEqualTo("assistant");
    }
}
