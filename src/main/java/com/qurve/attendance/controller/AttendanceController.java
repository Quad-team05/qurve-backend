package com.qurve.attendance.controller;

import com.qurve.attendance.dto.request.StudyTimeSaveRequestDto;
import com.qurve.attendance.dto.response.AttendanceResponseDto;
import com.qurve.attendance.dto.response.StudyTimeSaveResponseDto;
import com.qurve.attendance.service.AttendanceService;
import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "출석", description = "출석 체크 및 학습 시간 기록 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @Operation(summary = "출석 현황 조회", description = "이번 주 출석 상태와 연속 학습 일수를 조회합니다.")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> findOne(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.findOne(authentication.getName())));
    }

    @PostMapping
    @Operation(summary = "출석 체크", description = "오늘 출석을 기록하고 갱신된 출석 정보를 반환합니다.")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> save(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.save(authentication.getName())));
    }

    @PostMapping("/study-time")
    @Operation(summary = "학습 시간 저장", description = "오늘 학습 시간을 누적 저장합니다.")
    public ResponseEntity<ApiResponse<StudyTimeSaveResponseDto>> saveStudyTime(
            @Valid @RequestBody StudyTimeSaveRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.saveStudyTime(authentication.getName(), requestDto)));
    }
}
