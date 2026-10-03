package com.qurve.expression.controller;

import com.qurve.expression.dto.response.BasicExpressionResponseDto;
import com.qurve.expression.dto.response.TodayExpressionResponseDto;
import com.qurve.expression.service.BasicExpressionService;
import com.qurve.expression.service.ExpressionService;
import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expressions")
@RequiredArgsConstructor
@Tag(name = "표현", description = "오늘의 표현 및 초급 일본어 표현 조회 API")
public class ExpressionController {

    private final ExpressionService expressionService;
    private final BasicExpressionService basicExpressionService;

    @GetMapping("/today")
    @Operation(summary = "오늘의 표현 조회", description = "로그인한 사용자를 위한 오늘의 일본어 표현을 조회합니다.")
    public ResponseEntity<ApiResponse<TodayExpressionResponseDto>> findTodayExpression(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(expressionService.findTodayExpression(authentication.getName())));
    }

    @GetMapping("/basic")
    @Operation(summary = "초급 표현 조회", description = "초급 일본어 표현 전체를 CSV 순서대로 조회합니다.")
    public ResponseEntity<ApiResponse<List<BasicExpressionResponseDto>>> findBasicExpressions(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(basicExpressionService.findBasicExpressions(authentication.getName())));
    }
}