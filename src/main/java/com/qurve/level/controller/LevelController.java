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
    @Operation(summary = "레벨 테스트 사전 질문 조회", description = "레벨 테스트 시작 전 사용자에게 제시할 사전 질문을 조회합니다.")
    public ResponseEntity<ApiResponse<PreQuestionResponseDto>> getPreQuestions() {
        return ResponseEntity.ok(ApiResponse.success(levelService.getPreQuestions()));
    }

    @PostMapping("/test")
    @Operation(summary = "레벨 테스트 문제 조회", description = "사전 질문 응답을 기준으로 레벨 테스트 문제를 조회합니다.")
    public ResponseEntity<ApiResponse<LevelTestResponseDto>> getLevelTestQuestions(@Validated @RequestBody LevelTestRequestDto levelTestRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(levelService.getLevelTestQuestions(levelTestRequestDto)));
    }

    @PostMapping("/test/result")
    @Operation(summary = "레벨 테스트 결과 계산", description = "제출한 답안을 채점하고 예상 레벨 결과를 반환합니다.")
    public ResponseEntity<ApiResponse<LevelTestResultResponseDto>> levelTestResult(@Validated @RequestBody LevelTestResultRequestDto levelTestResultRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(levelService.levelTestResult(levelTestResultRequestDto)));
    }

    @PostMapping("/save")
    @Operation(summary = "사용자 레벨 저장", description = "레벨 테스트 결과 또는 선택한 학습 레벨을 사용자 정보에 저장합니다.")
    public ResponseEntity<ApiResponse<Void>> saveLevel(@Validated @RequestBody SaveLevelRequestDto levelSaveRequestDto) {
        levelService.saveLevel(levelSaveRequestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
