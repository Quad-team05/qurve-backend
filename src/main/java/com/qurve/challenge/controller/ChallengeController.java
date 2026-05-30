package com.qurve.challenge.controller;

import com.qurve.challenge.dto.request.ChallengeCreateRequestDto;
import com.qurve.challenge.dto.response.ChallengeCreateResponseDto;
import com.qurve.challenge.dto.response.ChallengeGoalTypeResponseDto;
import com.qurve.challenge.dto.response.ChallengeManageResponseDto;
import com.qurve.challenge.service.ChallengeService;
import com.qurve.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/goal-types")
    public ResponseEntity<ApiResponse<List<ChallengeGoalTypeResponseDto>>> getGoalTypes() {
        return ResponseEntity.ok(ApiResponse.success(challengeService.getGoalTypes()));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChallengeManageResponseDto>>> getMyChallenges(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        challengeService.getMyChallenges(authentication.getName())
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChallengeCreateResponseDto>> createChallenge(
            @Valid @RequestBody ChallengeCreateRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        challengeService.createChallenge(requestDto, authentication.getName())
                )
        );
    }
}