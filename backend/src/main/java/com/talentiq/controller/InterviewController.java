package com.talentiq.controller;

import com.talentiq.dto.request.InterviewGenerateRequest;
import com.talentiq.dto.response.InterviewResponse;
import com.talentiq.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/generate")
    public ResponseEntity<InterviewResponse> generate(@Valid @RequestBody InterviewGenerateRequest request) {
        return ResponseEntity.ok(interviewService.generate(request));
    }
}
