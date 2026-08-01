package com.talentiq.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.exception.AiServiceException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin, shared HTTP client for Groq's OpenAI-compatible {@code chat/completions} API. Centralizes
 * request/response plumbing (auth, timeouts, JSON envelope handling, safety-block detection, and
 * error translation) so that both the AI analysis engine ({@link com.talentiq.ai.impl.GroqAiServiceImpl})
 * and the chat assistant ({@code AssistantServiceImpl}) reuse a single code path.
 *
 * <p>Fails fast at startup if {@code app.ai.groq.api-key} (env var {@code GROQ_API_KEY}) is
 * blank, since every AI-backed feature in the app depends on it.
 */
@Component
public class GroqClient {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final int CONNECT_TIMEOUT_MS = 20_000;
    private static final int READ_TIMEOUT_MS = 20_000;

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GroqClient(@Value("${app.ai.groq.api-key:}") String apiKey,
                       @Value("${app.ai.groq.model:llama-3.3-70b-versatile}") String model) {
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }

    @PostConstruct
    void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY environment variable is not set. TalentIQ AI's resume analysis, "
                            + "matching, interview generation, skill-gap, career recommendation, and chat "
                            + "assistant features all require a Groq API key. Get a free key (no credit card "
                            + "required) from the Groq console (https://console.groq.com/keys) and set it as "
                            + "the GROQ_API_KEY environment variable before starting the application.");
        }
    }

    /** A single turn in a chat-style conversation, e.g. {@code new ChatTurn("user", "hello")}. */
    public record ChatTurn(String role, String text) {
    }

    /**
     * Calls Groq requesting a single-turn, JSON-only response (via {@code response_format:
     * {"type": "json_object"}}), using a low temperature suited to analytical/scoring tasks
     * (resume analysis, matching, skill gap) where consistency matters more than variety.
     */
    public String generateJson(String prompt) {
        return generateJson(prompt, 0.4);
    }

    /**
     * Same as {@link #generateJson(String)} but with an explicit temperature — pass something
     * higher (e.g. 0.9-1.0) for creative/generative tasks (interview questions, career ideas)
     * that should vary from call to call rather than converging on the same near-deterministic
     * output every time at low temperature.
     */
    public String generateJson(String prompt, double temperature) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message("user", prompt));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", temperature);
        body.put("response_format", Map.of("type", "json_object"));

        return call(body);
    }

    /**
     * Calls Groq for a free-form, multi-turn plain-text chat reply. {@code systemInstruction}
     * sets the assistant's persona/behavior, {@code history} is the prior conversation turns
     * (role {@code "user"} or {@code "assistant"}), and {@code userMessage} is the newest message.
     */
    public String generateChatReply(String systemInstruction, List<ChatTurn> history, String userMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();
        if (systemInstruction != null && !systemInstruction.isBlank()) {
            messages.add(message("system", systemInstruction));
        }
        if (history != null) {
            for (ChatTurn turn : history) {
                String role = "assistant".equalsIgnoreCase(turn.role()) ? "assistant" : "user";
                messages.add(message(role, turn.text() == null ? "" : turn.text()));
            }
        }
        messages.add(message("user", userMessage == null ? "" : userMessage));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.6);

        return call(body);
    }

    private Map<String, Object> message(String role, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content == null ? "" : content);
        return message;
    }

    private String call(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(API_URL, requestEntity, String.class);
        } catch (HttpStatusCodeException ex) {
            throw new AiServiceException(
                    "Groq API request failed with HTTP " + ex.getStatusCode().value()
                            + ": " + safeBody(ex.getResponseBodyAsString()), ex);
        } catch (ResourceAccessException ex) {
            throw new AiServiceException(
                    "Groq API request timed out or the network was unreachable: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            throw new AiServiceException("Groq API request failed: " + ex.getMessage(), ex);
        }

        HttpStatusCode status = response.getStatusCode();
        if (!status.is2xxSuccessful()) {
            throw new AiServiceException("Groq API returned a non-success status: " + status.value());
        }

        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiServiceException("Groq API returned an empty response body.");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (Exception ex) {
            throw new AiServiceException("Groq API returned a response that could not be parsed as JSON.", ex);
        }

        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new AiServiceException("Groq API returned no choices in its response.");
        }

        JsonNode firstChoice = choices.get(0);
        String finishReason = firstChoice.path("finish_reason").asText("");
        if ("content_filter".equals(finishReason)) {
            throw new AiServiceException("Groq blocked the response for safety/content-filter reasons ("
                    + finishReason + ").");
        }

        JsonNode content = firstChoice.path("message").path("content");
        if (content.isMissingNode() || content.isNull() || content.asText().isEmpty()) {
            throw new AiServiceException("Groq API response did not contain any text content.");
        }

        return content.asText();
    }

    private String safeBody(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}
