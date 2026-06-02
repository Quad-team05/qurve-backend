package com.qurve.level.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.level.dto.response.PreQuestionResponseDto;
import com.qurve.level.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
