package com.talentiq.exception;

/**
 * Thrown when the underlying AI provider (Groq) call fails: network error, non-2xx response,
 * a safety/content block, a timeout, or a malformed/unparseable response body. Mapped to
 * HTTP 502 Bad Gateway by {@link GlobalExceptionHandler} since the failure originates in an
 * upstream dependency rather than the client's request.
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
