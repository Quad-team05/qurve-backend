package com.qurve.global.security;

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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OAuth2 로그인 성공 핸들러
 *
 * * 소셜 로그인 성공 시 사용자 정보를 조회하거나 자동 회원가입 처리 후
 * JWT Access Token / Refresh Token을 발급하여 응답으로 반환한다.
 *
 * * 이메일 기준으로 기존 회원 여부를 확인하며,
 * 신규 사용자인 경우 자동으로 회원가입 처리한다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
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

        // 구글에서 반환된 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 이메일 기준으로 기존 회원 조회, 없으면 자동 회원가입 처리
        User user = userRepository.findByEmail(email)
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

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"accessToken\":\"" + accessToken + "\",\"refreshToken\":\"" + refreshToken + "\"}"
        );
    }
}