package com.qurve.expression.service;

import com.qurve.expression.dto.response.BasicExpressionResponseDto;
import com.qurve.expression.repository.BasicExpressionRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicExpressionService {

    private final UserRepository userRepository;
    private final BasicExpressionRepository basicExpressionRepository;

    /**
     * 초급 표현 전체 조회
     *
     * @param loginId 로그인 ID
     * @return CSV 순서로 정렬된 초급 표현 목록
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public List<BasicExpressionResponseDto> findBasicExpressions(String loginId) {
        validateUser(loginId);

        return basicExpressionRepository
                .findAllByOrderByOrderNumberAsc()
                .stream()
                .map(BasicExpressionResponseDto::from)
                .toList();
    }

    private void validateUser(String loginId) {
        if (!userRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}