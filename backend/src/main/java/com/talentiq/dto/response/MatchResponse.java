package com.talentiq.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponse {
    private Double matchScore;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
}
