package com.talentiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchRequest {

    @NotNull(message = "resumeId is required")
    private Long resumeId;

    @NotBlank(message = "jobDescription is required")
    private String jobDescription;
}
