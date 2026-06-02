package com.qurve.learning.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.learning.dto.response.TodayLearningResponseDto;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningService {

    private static final String TODAY_LEARNING_CATEGORY = "문자/어휘";
    private static final String TODAY_LEARNING_TITLE = "문맥규정";
    private static final int TODAY_LEARNING_TOTAL_QUESTION_COUNT = 20;
    private static final int TODAY_LEARNING_ESTIMATED_MINUTES = 10;

    private final UserRepository userRepository;

    /**
     * 오늘의 학습 카드 조회
     *
     * * 메인페이지 카드에 노출할 오늘의 학습 영역, 문항 수,
     * 예상 소요 시간을 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 오늘의 학습 카드 응답 정보
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public TodayLearningResponseDto findTodayLearning(String loginId) {
        validateUser(loginId);

        // TODO: 학습 콘텐츠 API 구현 후 사용자 레벨과 오늘 날짜를 기준으로 실제 학습 데이터를 조회하도록 변경 예정
        return TodayLearningResponseDto.of(
                TODAY_LEARNING_CATEGORY,
                TODAY_LEARNING_TITLE,
                TODAY_LEARNING_TOTAL_QUESTION_COUNT,
                TODAY_LEARNING_ESTIMATED_MINUTES
        );
    }

    private void validateUser(String loginId) {
        if (!userRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
