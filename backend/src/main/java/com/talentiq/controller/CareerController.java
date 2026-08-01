package com.talentiq.controller;

import com.talentiq.dto.response.CareerRecommendationResponse;
import com.talentiq.service.CareerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @GetMapping("/recommendations/{resumeId}")
    public ResponseEntity<List<CareerRecommendationResponse>> recommendations(@PathVariable Long resumeId) {
        return ResponseEntity.ok(careerService.recommend(resumeId));
    }
}
