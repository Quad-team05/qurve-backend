package com.qurve.user.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.LearningGoal;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.enums.LearningStage;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.dto.request.LearningLanguageRequestDto;
import com.qurve.user.dto.request.LearningProfileRequestDto;
import com.qurve.user.dto.request.UserPasswordChangeRequestDto;
import com.qurve.user.dto.request.UserProfileUpdateRequestDto;
import com.qurve.user.dto.response.LearningLanguageResponseDto;
import com.qurve.user.dto.response.LearningProfileResponseDto;
import com.qurve.user.dto.response.UserProfileResponseDto;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 회원 정보 조회
     *
     * * 인증된 사용자의 로그인 ID를 기준으로 회원 정보를 조회한다.
     *
     * @param loginId 로그인 ID
     * @return 회원 정보 응답
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public UserProfileResponseDto findOne(String loginId) {
        User user = userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserProfileResponseDto.from(user);
    }

    /**
     * 회원 정보 수정
     *
     * * PATCH 요청으로 전달된 값만 선택적으로 반영해
     * 인증된 사용자의 프로필 정보를 갱신한다.
     *
     * @param requestDto 회원 정보 수정 요청
     * @param loginId 로그인 ID
     * @return 수정된 회원 정보 응답
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    @Transactional
    public UserProfileResponseDto update(UserProfileUpdateRequestDto requestDto, String loginId) {
        User user = userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateProfile(requestDto.getName(), requestDto.getNickname());

        return UserProfileResponseDto.from(user);
    }

    @Transactional
    public void changePassword(UserPasswordChangeRequestDto requestDto, String loginId) {
        User user = userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    @Transactional
    public LearningProfileResponseDto updateLearningProfile(LearningProfileRequestDto requestDto, String loginId) {
        User user = userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LearningLanguage language = user.getLearningLanguage();
        LearningGoal goal = requestDto.getLearningGoal();
        LearningStage stage = requestDto.getLearningStage();

        validateLearningProfile(user, language, goal, stage);
        user.updateLearningProfile(goal, stage);

        return LearningProfileResponseDto.from(user);
    }

    private void validateLearningProfile(User user, LearningLanguage language, LearningGoal goal, LearningStage stage) {
        if (!goal.supports(language)) {
            throw new BusinessException(ErrorCode.INVALID_LEARNING_GOAL);
        }

        if (goal == LearningGoal.DAILY_LIFE) {
            if (stage != null)
                throw new BusinessException(ErrorCode.INVALID_LEARNING_STAGE);

            if (user.getCurrentLevel() == null)
                throw new BusinessException(ErrorCode.LEVEL_TEST_REQUIRED);

            return;
        }

        if (stage == null)
            throw new BusinessException(ErrorCode.LEARNING_STAGE_REQUIRED);

        if (!stage.matches(language, goal))
            throw new BusinessException(ErrorCode.INVALID_LEARNING_STAGE);
    }

    /**
     * 학습 언어 변경
     *
     * * 사용자가 학습할 언어(일본어/영어)를 선택하거나 변경한다.
     * * 언어를 전환해도 기존 학습 기록은 그대로 유지되며, 현재 선택된 언어 값만 갱신된다.
     *
     * @param requestDto 변경할 학습 언어
     * @param loginId 로그인 ID
     * @return 변경된 학습 언어 정보
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    @Transactional
    public LearningLanguageResponseDto updateLearningLanguage(LearningLanguageRequestDto requestDto, String loginId) {
        User user = userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateLearningLanguage(requestDto.getLearningLanguage());

        return LearningLanguageResponseDto.of(user.getLearningLanguage());
    }
}
