package com.talentiq.controller;

import com.talentiq.dto.response.UserResponse;
import com.talentiq.security.AppUserPrincipal;
import com.talentiq.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getUserId()));
    }
}
