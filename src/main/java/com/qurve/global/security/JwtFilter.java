package com.qurve.global.security;

import com.qurve.global.exception.BusinessException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 *
 * * 클라이언트 요청 헤더에 포함된 JWT를 검증하고,
 * 유효한 토큰일 경우 Spring Security 인증 객체를 등록한다.
 *
 * * OncePerRequestFilter를 상속받아
 * 하나의 요청당 한 번만 필터가 실행되도록 구성한다.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return "OPTIONS".equals(request.getMethod())
                || "/api/auth/login".equals(path)
                || "/api/auth/signup".equals(path)
                || "/api/auth/check-id".equals(path)
                || "/api/auth/check-email".equals(path)
                || "/api/auth/signup/email/send".equals(path)
                || "/api/auth/email/verify".equals(path)
                || "/api/auth/signup/email/verify".equals(path)
                || "/api/auth/find-id".equals(path)
                || "/api/auth/password/email/send".equals(path)
                || "/api/auth/password/reset".equals(path)
                || "/api/auth/reissue".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        // 유효한 토큰일 때 Spring Security에 인증 정보 등록
        if (token != null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String loginId = jwtTokenProvider.getLoginId(token);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginId, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (BusinessException e) {
                // 유효하지 않은 토큰일 경우 인증 없이 통과
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 JWT 추출
     *
     * * "Bearer " 접두사를 제거한 실제 JWT 문자열을 반환한다.
     */
    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

}
