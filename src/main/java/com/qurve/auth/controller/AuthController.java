package com.qurve.auth.controller;

import com.qurve.auth.dto.request.*;
import com.qurve.auth.dto.response.*;
import com.qurve.auth.service.AuthService;
import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@Validated
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인, 이메일 인증, 계정 복구, 토큰 관리 API")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일 인증이 완료된 정보로 일반 회원가입을 진행합니다.")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.save(signupRequestDto)));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 검증하고 Access·Refresh 토큰을 발급합니다.")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(loginRequestDto)));
    }

    @GetMapping("/check-id")
    @Operation(summary = "아이디 중복 확인", description = "회원가입에 사용할 로그인 ID의 중복 여부를 확인합니다.")
    public ResponseEntity<ApiResponse<Void>> checkLoginId(@NotBlank @Size(min = 1, max = 30) @RequestParam("loginId") String loginId) {
        authService.checkLoginId(loginId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/signup/email/send")
    @Operation(summary = "회원가입 이메일 인증번호 발송", description = "회원가입에 사용할 이메일로 인증번호를 발송합니다.")
    public ResponseEntity<ApiResponse<SignupEmailResponseDto>> sendSignupEmail(@Valid @RequestBody SignupEmailRequestDto signupEmailRequestDto) {
        return ResponseEntity.ok(ApiResponse.success((authService.signupEmailSend(signupEmailRequestDto))));
    }

    @PostMapping({"/email/verify", "/signup/email/verify"})
    @Operation(summary = "이메일 인증번호 확인", description = "이메일과 인증번호를 검증해 인증 완료 상태를 처리합니다.")
    public ResponseEntity<ApiResponse<EmailVerifyResponseDto>> verifyEmail(@Valid @RequestBody EmailVerifyRequestDto emailVerifyRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.emailVerify(emailVerifyRequestDto)));
    }

    @PostMapping("/reissue")
    @Operation(summary = "Access 토큰 재발급", description = "Refresh 토큰으로 새로운 Access 토큰을 발급합니다.")
    public ResponseEntity<ApiResponse<TokenReissueResponseDto>> reissue(@Valid @RequestBody TokenReissueRequestDto tokenReissueRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.reissue(tokenReissueRequestDto)));
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자 계정을 소프트 삭제 처리합니다.")
    public ResponseEntity<ApiResponse<Void>> withdraw() {
        authService.withdraw();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기", description = "가입 정보로 로그인 ID를 조회합니다.")
    public ResponseEntity<ApiResponse<FindIdResponseDto>> findId(@Valid @RequestBody FindIdRequestDto findIdRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.findId(findIdRequestDto)));
    }

    @GetMapping("/check-email")
    @Operation(summary = "이메일 가입 여부 확인", description = "입력한 이메일의 가입 여부를 확인합니다.")
    public ResponseEntity<ApiResponse<Void>> checkEmail(@NotBlank @Email @RequestParam("email") String email) {
        authService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/password/email/send")
    @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정용 이메일 인증번호를 발송합니다.")
    public ResponseEntity<ApiResponse<PasswordEmailResponseDto>> sendPasswordEmail(@Valid @RequestBody PasswordEmailRequestDto passwordEmailRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.passwordEmailSend(passwordEmailRequestDto)));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정", description = "이메일 인증 후 새 비밀번호로 재설정합니다.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
        authService.resetPassword(resetPasswordRequestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
  
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그인한 사용자의 Refresh 토큰을 무효화합니다.")
    public ResponseEntity<ApiResponse<AuthLogoutResponseDto>> logout(Authentication authentication) {
        AuthLogoutResponseDto responseDto = authService.logout(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}
