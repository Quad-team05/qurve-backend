package com.qurve.xp.controller;

import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.xp.dto.response.TodayXpResponseDto;
import com.qurve.xp.dto.response.XpDailyResponseDto;
import com.qurve.xp.dto.response.XpStatResponseDto;
import com.qurve.xp.service.XpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/xp")
@Tag(name = "XP", description = "사용자 XP 통계 및 적립 현황 조회 API")
public class XpController {

    private final XpService xpService;

    @GetMapping("/stat")
    @Operation(summary = "XP 통계 조회", description = "누적 XP와 레벨 등 XP 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<XpStatResponseDto>> getXpStat(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(xpService.getXpStat(authentication.getName())));
    }

    @GetMapping("/weekly")
    @Operation(summary = "주간 XP 조회", description = "최근 일주일의 일별 XP 적립 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<List<XpDailyResponseDto>>> getXpWeekly(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(xpService.getWeeklyXp(authentication.getName())));
    }

    @GetMapping("/today")
    @Operation(summary = "오늘의 XP 조회", description = "오늘 적립한 XP와 적립 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<TodayXpResponseDto>> getTodayXp(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(xpService.getTodayXp(authentication.getName())));
    }
}
