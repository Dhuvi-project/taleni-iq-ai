package com.talentiq.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeSummaryResponse {
    private Long id;
    private String fileName;
    private Integer version;
    private Double atsScore;
    private Instant createdAt;
}
