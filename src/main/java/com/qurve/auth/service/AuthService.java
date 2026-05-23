package com.qurve.auth.service;

import com.qurve.auth.dto.request.SignupRequestDto;
import com.qurve.auth.dto.response.SignupResponseDto;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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

        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = dto.toEntity(encodedPassword);

        User savedUser = userRepository.save(user);

        return SignupResponseDto.from(savedUser);
    }
}
