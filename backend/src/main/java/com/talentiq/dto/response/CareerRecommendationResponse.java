package com.talentiq.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CareerRecommendationResponse {
    private String jobTitle;
    private String company;
    private String location;
    private Double matchScore;
    private Double salaryMinLPA;
    private Double salaryMaxLPA;
    private String marketDemand;
    private Double growthPercent;
    private List<String> requiredUpskilling;
}
