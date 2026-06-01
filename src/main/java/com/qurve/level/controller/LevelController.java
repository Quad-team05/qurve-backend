package com.qurve.level.controller;

import com.qurve.global.common.ApiResponse;
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
public class LevelController {
    private final LevelService levelService;

    @GetMapping("/pre-questions")
    public ResponseEntity<ApiResponse<PreQuestionResponseDto>> getPreQuestions() {
        return ResponseEntity.ok(ApiResponse.success(levelService.getPreQuestions()));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<LevelTestResponseDto>> getLevelTestQuestions(@Validated @RequestBody LevelTestRequestDto levelTestRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(levelService.getLevelTestQuestions(levelTestRequestDto)));
    }

    @PostMapping("/test/result")
    public ResponseEntity<ApiResponse<LevelTestResultResponseDto>> levelTestResult(@Validated @RequestBody LevelTestResultRequestDto levelTestResultRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(levelService.levelTestResult(levelTestResultRequestDto)));
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Void>> saveLevel(@Validated @RequestBody SaveLevelRequestDto levelSaveRequestDto) {
        levelService.saveLevel(levelSaveRequestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
