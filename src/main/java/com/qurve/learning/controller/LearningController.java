package com.qurve.learning.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.learning.dto.request.StudyTimeSaveRequestDto;
import com.qurve.learning.dto.response.StudyTimeSaveResponseDto;
import com.qurve.learning.dto.response.StudyTimeStatisticsResponseDto;
import com.qurve.learning.dto.response.TodayLearningResponseDto;
import com.qurve.learning.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learnings")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayLearningResponseDto>> findTodayLearning(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        learningService.findTodayLearning(authentication.getName())
                )
        );
    }

    @GetMapping("/study-time/statistics")
    public ResponseEntity<ApiResponse<StudyTimeStatisticsResponseDto>> findStudyTimeStatistics(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        learningService.findStudyTimeStatistics(authentication.getName())
                )
        );
    }

    @PostMapping("/study-time")
    public ResponseEntity<ApiResponse<StudyTimeSaveResponseDto>> saveStudyTime(
            @Valid @RequestBody StudyTimeSaveRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        learningService.saveStudyTime(authentication.getName(), requestDto)
                )
        );
    }
}
