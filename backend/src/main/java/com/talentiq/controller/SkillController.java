package com.talentiq.controller;

import com.talentiq.dto.response.SkillGapResponse;
import com.talentiq.service.SkillGapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillGapService skillGapService;

    @GetMapping("/gap/{resumeId}")
    public ResponseEntity<SkillGapResponse> gap(@PathVariable Long resumeId) {
        return ResponseEntity.ok(skillGapService.computeGap(resumeId));
    }
}
