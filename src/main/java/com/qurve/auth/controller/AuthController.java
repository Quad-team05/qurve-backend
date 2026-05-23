package com.qurve.auth.controller;

import com.qurve.auth.dto.request.SignupRequestDto;
import com.qurve.auth.dto.response.SignupResponseDto;
import com.qurve.auth.service.AuthService;
import com.qurve.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(signupRequestDto)));
    }
}
