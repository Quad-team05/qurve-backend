package com.qurve.xp.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
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
public class XpController {

    private final XpService xpService;

    @GetMapping("/stat")
    public ResponseEntity<ApiResponse<XpStatResponseDto>> getXpStat(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(xpService.getXpStat(authentication.getName())));
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<List<XpDailyResponseDto>>> getXpWeekly(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(xpService.getWeeklyXp(authentication.getName())));
    }
}
