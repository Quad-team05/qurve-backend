package com.qurve.auth.controller;

import com.qurve.auth.dto.request.EmailVerifyRequestDto;
import com.qurve.auth.dto.request.SignupEmailRequestDto;
import com.qurve.auth.dto.request.LoginRequestDto;
import com.qurve.auth.dto.request.SignupRequestDto;
import com.qurve.auth.dto.response.EmailVerifyResponseDto;
import com.qurve.auth.dto.response.SignupEmailResponseDto;
import com.qurve.auth.dto.response.LoginResponseDto;
import com.qurve.auth.dto.response.SignupResponseDto;
import com.qurve.auth.service.AuthService;
import com.qurve.global.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.save(signupRequestDto)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(loginRequestDto)));
    }

    @GetMapping("/check-id")
    public ResponseEntity<ApiResponse<Void>> checkLoginId(@NotBlank @Size(min = 1, max = 30) @RequestParam("loginId") String loginId) {
        authService.checkLoginId(loginId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/signup/email/send")
    public ResponseEntity<ApiResponse<SignupEmailResponseDto>> sendSignupEmail(@Valid @RequestBody SignupEmailRequestDto signupEmailRequestDto) {
        return ResponseEntity.ok(ApiResponse.success((authService.signupEmailSend(signupEmailRequestDto))));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<EmailVerifyResponseDto>> verifyEmail(@Valid @RequestBody EmailVerifyRequestDto emailVerifyRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.emailVerify(emailVerifyRequestDto)));
    }
}
