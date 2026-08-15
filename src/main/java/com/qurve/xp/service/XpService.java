package com.qurve.xp.service;

import com.qurve.attendance.domain.StudyStatistics;
import com.qurve.attendance.repository.StudyStatisticsRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.XpActionType;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.xp.domain.XpHistory;
import com.qurve.xp.dto.response.XpStatResponseDto;
import com.qurve.xp.repository.XpHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class XpService {

    private final XpHistoryRepository xpHistoryRepository;
    private final StudyStatisticsRepository studyStatisticsRepository;
    private final UserRepository userRepository;

    /**
     * XP 부여
     *
     * * 특정 액션에 대해 XP를 부여하고 기록한다.
     *
     * @param user 대상 유저
     * @param actionType XP 획득 액션 타입
     */
    @Transactional
    public void grantXp(User user, XpActionType actionType) {
        int xpAmount = getXpAmount(actionType);

        xpHistoryRepository.save(XpHistory.builder()
                .user(user)
                .actionType(actionType)
                .xpAmount(xpAmount)
                .earnedAt(LocalDateTime.now())
                .build());
    }

    // 액션 타입별 XP량 반환
    public int getXpAmount(XpActionType actionType) {
        return switch (actionType) {
            case DAILY_ATTENDANCE -> 10;
            case STREAK_3_DAYS -> 30;
            case STREAK_7_DAYS -> 70;
            case PROBLEM_CORRECT -> 10;
            case PROBLEM_SET_COMPLETE -> 30;
            case PROBLEM_SET_PERFECT -> 50;
            case WRONG_NOTE_COMPLETE -> 15;
            case WRONG_NOTE_CORRECT -> 20;
            case WORD_LEARN -> 5;
            case WORD_SET_COMPLETE -> 20;
            case WORD_BOOKMARK -> 3;
            case CHALLENGE_COMPLETE -> 100;
            case DAILY_GOAL_COMPLETE -> 50;
            case AI_COACH_FIRST -> 20;
            case AI_COACH_DAILY -> 5;
        };
    }

    /**
     * 레벨 및 XP 통계 조회
     *
     * * XP 기록을 전부 합산하여 누적 XP를 계산하고,
     * 누적 XP 기반으로 현재 레벨, 칭호, 다음 레벨까지 필요 XP를 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 레벨 및 XP 통계
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public XpStatResponseDto getXpStat(String loginId) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long totalXpValue = xpHistoryRepository.sumXpAmountByUser(user);
        int totalXp = Math.toIntExact(totalXpValue);

        int streakDays = studyStatisticsRepository.findByUser(user)
                .map(StudyStatistics::getStreakDays)
                .orElse(0);

        return XpStatResponseDto.of(totalXp, streakDays);
    }
}
