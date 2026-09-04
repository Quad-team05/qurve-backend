package com.qurve.learning.controller;

import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.qurve.learning.dto.request.StudyTimeSaveRequestDto;
import com.qurve.learning.dto.response.StudyTimeSaveResponseDto;
import com.qurve.learning.dto.response.StudyTimeStatisticsResponseDto;
import com.qurve.learning.dto.response.LearningMainResponseDto;
import com.qurve.learning.dto.response.TodayLearningResponseDto;
import com.qurve.learning.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/learnings")
@RequiredArgsConstructor
@Tag(name = "학습", description = "학습 메인, 오늘의 학습, 학습 시간 통계 API")
public class LearningController {

    private final LearningService learningService;

    /**
     * 학습 메인 화면 조회
     */
    @GetMapping("/main")
    @Operation(summary = "학습 메인 조회", description = "학습 목적, 레벨, 챌린지, 오늘의 학습, 오답노트 및 단어 학습 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<LearningMainResponseDto>> findMain(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                learningService.findMain(authentication.getName())
        ));
    }

    @GetMapping("/today")
    @Operation(summary = "오늘의 학습 조회", description = "사용자 레벨과 기준 날짜에 따라 선택된 오늘의 문제 세트를 조회합니다.")
    public ResponseEntity<ApiResponse<TodayLearningResponseDto>> findTodayLearning(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        learningService.findTodayLearning(authentication.getName(), date)
                )
        );
    }

    @GetMapping("/study-time/statistics")
    @Operation(summary = "학습 시간 통계 조회", description = "이번 주 일별 학습 시간과 오늘·주간 누적 시간을 조회합니다.")
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
    @Operation(summary = "학습 시간 누적 저장", description = "학습 시간을 날짜별 기록과 전체 학습 통계에 누적합니다.")
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
