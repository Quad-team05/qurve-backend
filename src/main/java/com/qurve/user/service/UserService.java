package com.qurve.user.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.dto.request.LearningProfileRequestDto;
import com.qurve.user.dto.response.LearningProfileResponseDto;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public LearningProfileResponseDto updateLearningProfile(
            LearningProfileRequestDto requestDto,
            String loginId
    ) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateLearningProfile(
                requestDto.getLearningGoal(),
                requestDto.getCurrentLevel()
        );

        return LearningProfileResponseDto.from(user);
    }
}