package com.qurve.badge.controller;

import com.qurve.badge.dto.response.BadgeListResponseDto;
import com.qurve.badge.service.BadgeService;
import com.qurve.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<ApiResponse<BadgeListResponseDto>> findAll(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(badgeService.findAll(authentication.getName())));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<BadgeListResponseDto>> evaluate(Authentication authentication) {
        badgeService.evaluate(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(badgeService.findAll(authentication.getName())));
    }
}
