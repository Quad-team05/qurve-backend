package com.qurve.learning.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.learning.dto.response.LearningMainResponseDto;
import com.qurve.learning.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/main")
    public ResponseEntity<ApiResponse<LearningMainResponseDto>> getLearningMain(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        learningService.getLearningMain(authentication.getName())
                )
        );
    }
}