package com.qurve.badge.controller;

import com.qurve.badge.dto.response.BadgeListResponseDto;
import com.qurve.badge.service.BadgeService;
import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "배지", description = "사용자 배지 조회 및 달성 조건 평가 API")
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    @Operation(summary = "내 배지 목록 조회", description = "로그인한 사용자가 획득한 배지와 진행 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<BadgeListResponseDto>> findAll(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(badgeService.findAll(authentication.getName())));
    }

    @PostMapping("/evaluate")
    @Operation(summary = "배지 조건 평가", description = "현재 학습 데이터를 기준으로 배지 달성 여부를 평가합니다.")
    public ResponseEntity<ApiResponse<BadgeListResponseDto>> evaluate(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(badgeService.evaluateAndFindAll(authentication.getName())));
    }
}
