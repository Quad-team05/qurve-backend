package com.qurve.global.security;

import com.qurve.badge.service.BadgeService;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.global.enums.Role;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * OAuth2 로그인 성공 핸들러
 *
 * * 소셜 로그인 성공 시 사용자 정보를 조회하거나 자동 회원가입 처리 후
 * JWT Access Token / Refresh Token을 발급하여 응답으로 반환한다.
 *
 * * 구글: email, name 속성으로 사용자 정보 추출
 * * 카카오: kakao_account.email, kakao_account.profile.nickname으로 사용자 정보 추출
 * * 네이버: response.email, response.name으로 사용자 정보 추출
 *
 * * 이메일 기준으로 기존 회원 여부를 확인하며,
 * 신규 사용자인 경우 자동으로 회원가입 처리한다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final BadgeService badgeService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 소셜 로그인 성공 시 JWT 발급
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authentication 인증 정보
     * @throws IOException 응답 작성 실패 시
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email;
        String name;

        // 카카오 로그인인 경우 kakao_account에서 사용자 정보 추출
        if (oAuth2User.getAttribute("kakao_account") != null) {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
        } // 네이버 로그인인 경우 response에서 사용자 정보 추출
        else if (oAuth2User.getAttribute("response") != null) {
            Map<String, Object> naverResponse = oAuth2User.getAttribute("response");
            email = (String) naverResponse.get("email");
            name = (String) naverResponse.get("name");
        } // 구글 로그인인 경우 최상위 속성에서 사용자 정보 추출
        else {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        }

        // 이메일 기준으로 기존 회원 조회, 없으면 자동 회원가입 처리
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    if (existingUser.isDeleted()) {
                        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                    }
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .loginId(email)
                        .email(email)
                        .name(name)
                        .nickname(name)
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(Role.USER)
                        .emailVerified(true)
                        .build()));

        // JWT Access Token / Refresh Token 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken();
        user.updateRefreshToken(refreshToken, LocalDateTime.now().plusDays(7));
        userRepository.save(user);
        badgeService.evaluate(user);

        String redirectUrl = UriComponentsBuilder
                .fromUriString("qurvefrontend://auth/social-callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
