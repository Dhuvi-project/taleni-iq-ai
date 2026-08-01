package com.talentiq.controller;

import com.talentiq.dto.request.MatchRequest;
import com.talentiq.dto.response.MatchResponse;
import com.talentiq.service.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchingService matchingService;

    @PostMapping
    public ResponseEntity<MatchResponse> match(@Valid @RequestBody MatchRequest request) {
        return ResponseEntity.ok(matchingService.match(request));
    }
}
