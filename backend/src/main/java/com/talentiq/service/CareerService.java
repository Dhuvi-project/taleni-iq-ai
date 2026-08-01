package com.talentiq.service;

import com.talentiq.dto.response.CareerRecommendationResponse;

import java.util.List;

public interface CareerService {
    List<CareerRecommendationResponse> recommend(Long resumeId);
}
