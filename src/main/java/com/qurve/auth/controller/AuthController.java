package com.qurve.auth.controller;

import com.qurve.auth.dto.request.*;
import com.qurve.auth.dto.response.*;
import com.qurve.auth.service.AuthService;
import com.qurve.global.common.ApiResponse;
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

    @PostMapping({"/email/verify", "/signup/email/verify"})
    public ResponseEntity<ApiResponse<EmailVerifyResponseDto>> verifyEmail(@Valid @RequestBody EmailVerifyRequestDto emailVerifyRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.emailVerify(emailVerifyRequestDto)));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenReissueResponseDto>> reissue(@Valid @RequestBody TokenReissueRequestDto tokenReissueRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.reissue(tokenReissueRequestDto)));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw() {
        authService.withdraw();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<FindIdResponseDto>> findId(@Valid @RequestBody FindIdRequestDto findIdRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.findId(findIdRequestDto)));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Void>> checkEmail(@NotBlank @Email @RequestParam("email") String email) {
        authService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/password/email/send")
    public ResponseEntity<ApiResponse<PasswordEmailResponseDto>> sendPasswordEmail(@Valid @RequestBody PasswordEmailRequestDto passwordEmailRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.passwordEmailSend(passwordEmailRequestDto)));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
        authService.resetPassword(resetPasswordRequestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
  
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<AuthLogoutResponseDto>> logout(Authentication authentication) {
        AuthLogoutResponseDto responseDto = authService.logout(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}
