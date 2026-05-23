package com.qurve.auth.service;

import com.qurve.auth.dto.request.LoginRequestDto;
import com.qurve.auth.dto.request.SignupRequestDto;
import com.qurve.auth.dto.response.LoginResponseDto;
import com.qurve.auth.dto.response.SignupResponseDto;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.global.security.JwtTokenProvider;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     *
     * * 로그인 ID와 이메일은 유저를 식별하는 고유 값이므로
     * 중복 가입을 방지하기 위해 저장 전에 존재 여부를 검증한다.
     *
     * * 비밀번호는 보안을 위해 BCrypt 방식으로 암호화 후 저장한다.
     *
     * @param dto 회원가입 요청 정보
     * @return 회원가입 응답 정보
     * @throws BusinessException 로그인 ID 또는 이메일이 이미 존재하는 경우
     */
    @Transactional
    public SignupResponseDto signup(SignupRequestDto dto) {

        // 로그인 ID 중복 여부 검증
        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 이메일 중복 여부 검증
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = dto.toEntity(encodedPassword);

        User savedUser = userRepository.save(user);

        return SignupResponseDto.from(savedUser);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto dto) {

        // 존재하는 유저인지 검증
        User user = userRepository.findByLoginId(dto.getLoginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken();
        // 객체에 Refresh Token + 만료시간 저장
        user.updateRefreshToken(refreshToken, LocalDateTime.now().plusDays(7));

        return LoginResponseDto.from(user, accessToken, refreshToken);
    }

}
