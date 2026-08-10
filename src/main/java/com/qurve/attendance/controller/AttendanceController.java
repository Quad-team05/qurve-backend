package com.qurve.attendance.controller;

import com.qurve.attendance.dto.request.StudyTimeSaveRequestDto;
import com.qurve.attendance.dto.response.AttendanceResponseDto;
import com.qurve.attendance.dto.response.StudyTimeSaveResponseDto;
import com.qurve.attendance.service.AttendanceService;
import com.qurve.global.common.ApiResponse;
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
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> findOne(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.findOne(authentication.getName())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> save(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.save(authentication.getName())));
    }

    @PostMapping("/study-time")
    public ResponseEntity<ApiResponse<StudyTimeSaveResponseDto>> saveStudyTime(
            @Valid @RequestBody StudyTimeSaveRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.saveStudyTime(authentication.getName(), requestDto)));
    }
}
