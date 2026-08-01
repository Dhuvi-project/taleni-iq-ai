package com.talentiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewGenerateRequest {

    @NotBlank(message = "role is required")
    private String role;

    /** One of TECHNICAL, BEHAVIORAL, CODING, ALL. Defaults to ALL when omitted. */
    private String type;
}
