package com.talentiq.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeAnalysisResponse {
    private Long resumeId;
    private Double atsScore;
    private Map<String, Double> sectionScores;
    private Double keywordDensity;
    private Integer actionVerbCount;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> extractedSkills;
}
