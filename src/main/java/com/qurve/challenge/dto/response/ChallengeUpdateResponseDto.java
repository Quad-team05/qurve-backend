package com.qurve.challenge.dto.response;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 챌린지 수정 결과입니다.
 */
@Getter
@Builder
public class ChallengeUpdateResponseDto {

    private Long challengeId;
    private String title;
    private ChallengeGoalType goalType;
    private Integer targetValue;
    private Integer currentValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private ChallengeStatus status;

    public static ChallengeUpdateResponseDto from(Challenge challenge) {
        return ChallengeUpdateResponseDto.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .goalType(challenge.getGoalType())
                .targetValue(challenge.getTargetValue())
                .currentValue(challenge.getCurrentValue())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .status(challenge.getStatus())
                .build();
    }
}
