package com.talentiq.controller;

import com.talentiq.dto.response.ResumeAnalysisResponse;
import com.talentiq.dto.response.ResumeSummaryResponse;
import com.talentiq.dto.response.ResumeUploadResponse;
import com.talentiq.security.AppUserPrincipal;
import com.talentiq.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResumeUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "userId", required = false) Long userId,
                                                         @AuthenticationPrincipal AppUserPrincipal principal) {
        ResumeUploadResponse response = resumeService.upload(file, userId, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ResumeSummaryResponse>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(resumeService.listByUser(userId));
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<ResumeAnalysisResponse> getAnalysis(@PathVariable Long id) {
        return ResponseEntity.ok(resumeService.getAnalysis(id));
    }
}
