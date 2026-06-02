package com.qurve.expression.controller;

import com.qurve.expression.dto.response.TodayExpressionResponseDto;
import com.qurve.expression.service.ExpressionService;
import com.qurve.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expressions")
@RequiredArgsConstructor
public class ExpressionController {

    private final ExpressionService expressionService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayExpressionResponseDto>> findTodayExpression(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        expressionService.findTodayExpression(authentication.getName())
                )
        );
    }
}
