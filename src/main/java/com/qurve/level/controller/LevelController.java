package com.qurve.level.controller;

import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.qurve.level.dto.request.LevelTestRequestDto;
import com.qurve.level.dto.request.LevelTestResultRequestDto;
import com.qurve.level.dto.request.SaveLevelRequestDto;
import com.qurve.level.dto.response.LevelTestResponseDto;
import com.qurve.level.dto.response.LevelTestResultResponseDto;
import com.qurve.level.dto.response.PreQuestionResponseDto;
import com.qurve.level.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/level")
@RequiredArgsConstructor
@Tag(name = "레벨 테스트", description = "레벨 테스트 문제 조회, 채점, 결과 저장 API")
public class LevelController {
    private final LevelService levelService;

    @GetMapping("/pre-questions")
    @Operation(summary = "레벨 테스트 사전 질문 조회", description = "로그인한 사용자의 학습 언어에 맞는 사전 질문을 조회합니다.")
    public ResponseEntity<ApiResponse<PreQuestionResponseDto>> getPreQuestions(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(levelService.getPreQuestions(authentication.getName())));
    }

    @PostMapping("/test")
    @Operation(summary = "레벨 테스트 문제 조회", description = "로그인한 사용자의 학습 언어와 사전 질문 응답을 기준으로 레벨 테스트 문제를 조회합니다.")
    public ResponseEntity<ApiResponse<LevelTestResponseDto>> getLevelTestQuestions(@Validated @RequestBody LevelTestRequestDto levelTestRequestDto, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(levelService.getLevelTestQuestions(levelTestRequestDto, authentication.getName())));
    }

    @PostMapping("/test/result")
    @Operation(summary = "레벨 테스트 결과 계산", description = "로그인한 사용자의 학습 언어에 맞는 문제를 기준으로 제출한 답안을 채점합니다.")
    public ResponseEntity<ApiResponse<LevelTestResultResponseDto>> levelTestResult(@Validated @RequestBody LevelTestResultRequestDto levelTestResultRequestDto, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(levelService.levelTestResult(levelTestResultRequestDto, authentication.getName())));
    }

    @PostMapping("/save")
    @Operation(summary = "사용자 레벨 저장", description = "레벨 테스트 결과 또는 선택한 학습 레벨을 사용자 정보에 저장합니다.")
    public ResponseEntity<ApiResponse<Void>> saveLevel(@Validated @RequestBody SaveLevelRequestDto levelSaveRequestDto) {
        levelService.saveLevel(levelSaveRequestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
