package com.qurve.challenge.controller;

import com.qurve.challenge.dto.request.ChallengeCreateRequestDto;
import com.qurve.challenge.dto.response.ChallengeCreateResponseDto;
import com.qurve.challenge.dto.response.ChallengeMainResponseDto;
import com.qurve.challenge.dto.response.ChallengeManagementResponseDto;
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

    @GetMapping("/main")
    public ResponseEntity<ApiResponse<List<ChallengeMainResponseDto>>> findAllForMain(
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(challengeService.findAllForMain(authentication.getName())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ChallengeManagementResponseDto>> findManagement(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        challengeService.findManagement(authentication.getName())
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
