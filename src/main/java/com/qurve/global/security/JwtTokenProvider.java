package com.qurve.global.security;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 생성 및 검증 담당 Provider
 *
 * * Access Token / Refresh Token 생성,
 * 토큰 유효성 검증,
 * 토큰 내부 정보 추출 기능을 담당한다.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Access Token 생성
     *
     * * 로그인 ID와 권한(Role) 정보를 포함한 JWT를 생성한다.
     *
     * @param loginId 로그인 ID
     * @param role 사용자 권한
     * @return 생성된 Access Token
     */
    public String createAccessToken(String loginId , String role) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(loginId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     *
     * * 재발급 전용 JWT를 생성한다.
     *
     * @return 생성된 Refresh Token
     */
    public String createRefreshToken() {
        Date now = new Date();

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 유효성 검증
     *
     * * JWT 서명 및 만료 여부를 검증한다.
     *
     * @param token JWT 문자열
     * @return 유효 여부
     * @throws BusinessException JWT 검증 실패 시 발생
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.EMPTY_TOKEN);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    /**
     * JWT 내부에서 로그인 ID 추출
     *
     * * JWT Subject에 저장된 로그인 ID를 반환한다.
     *
     * @param token JWT 문자열
     * @return 로그인 ID
     */
    public String getLoginId(String token) {
        return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
    }

}
