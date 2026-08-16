package com.qurve.user.controller;

import com.qurve.global.common.ApiResponse;
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
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> findOne(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(userService.findOne(authentication.getName())));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> update(
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(requestDto, authentication.getName())));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody UserPasswordChangeRequestDto requestDto,
            Authentication authentication
    ) {
        userService.changePassword(requestDto, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/learning-profile")
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
