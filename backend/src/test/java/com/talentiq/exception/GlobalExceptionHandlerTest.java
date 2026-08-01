package com.talentiq.exception;

import com.talentiq.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAiServiceException_returns502WithErrorShape() {
        when(request.getRequestURI()).thenReturn("/api/resumes/1/analysis");

        AiServiceException ex = new AiServiceException("Groq API request timed out or the network was unreachable: timeout");
        ResponseEntity<ErrorResponse> response = handler.handleAiServiceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(502);
        assertThat(body.getError()).isEqualTo("Bad Gateway");
        assertThat(body.getMessage()).isEqualTo(ex.getMessage());
        assertThat(body.getPath()).isEqualTo("/api/resumes/1/analysis");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    void handleNoResourceFound_returns404NotAGeneric500() {
        when(request.getRequestURI()).thenReturn("/api/notifications");
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());

        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "api/notifications");
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getPath()).isEqualTo("/api/notifications");
    }
}
