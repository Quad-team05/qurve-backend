package com.qurve.learning.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.learning.dto.response.LearningMainResponseDto;
import com.qurve.learning.dto.response.TodayLearningResponseDto;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningService {

    private final UserRepository userRepository;

    public LearningMainResponseDto getLearningMain(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return LearningMainResponseDto.from(user);
    }

    public TodayLearningResponseDto getTodayLearningInfo(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return TodayLearningResponseDto.from(user);
    }
}