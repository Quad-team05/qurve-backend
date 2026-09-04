package com.qurve.user.controller;

import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.qurve.user.dto.request.LearningProfileRequestDto;
import com.qurve.user.dto.request.UserPasswordChangeRequestDto;
import com.qurve.user.dto.request.UserProfileUpdateRequestDto;
import com.qurve.user.dto.response.LearningProfileResponseDto;
import com.qurve.user.dto.response.UserProfileResponseDto;
import com.qurve.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "마이페이지 사용자 정보 및 학습 프로필 관리 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "회원 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> findOne(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(userService.findOne(authentication.getName())));
    }

    @PatchMapping("/profile")
    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> update(
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(requestDto, authentication.getName())));
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody UserPasswordChangeRequestDto requestDto,
            Authentication authentication
    ) {
        userService.changePassword(requestDto, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/learning-profile")
    @Operation(summary = "학습 목적·단계 설정", description = "사용자의 학습 목적, 현재 레벨, 목표 언어를 저장합니다.")
    public ResponseEntity<ApiResponse<LearningProfileResponseDto>> updateLearningProfile(
            @Valid @RequestBody LearningProfileRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        userService.updateLearningProfile(requestDto, authentication.getName())
                )
        );
    }
}
