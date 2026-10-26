package com.qurve.challenge.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 챌린지 관리 화면의 전체 현황과 목록을 함께 반환합니다.
 */
@Getter
@Builder
public class ChallengeManagementResponseDto {

    private Integer streakDays;
    private Integer totalProgressRate;
    private Integer activeChallengeCount;
    private Integer completedChallengeCount;
    private List<ChallengeManageResponseDto> activeChallenges;
    private List<ChallengeManageResponseDto> completedChallenges;
}
