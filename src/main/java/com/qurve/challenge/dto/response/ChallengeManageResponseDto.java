package com.qurve.challenge.dto.response;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class ChallengeManageResponseDto {

    private Long challengeId;
    private String title;
    private ChallengeGoalType goalType;
    private Integer targetValue;
    private Integer currentValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private ChallengeStatus status;
    private Integer progressRate;

    public static ChallengeManageResponseDto from(Challenge challenge, int progressRate) {
        return ChallengeManageResponseDto.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .goalType(challenge.getGoalType())
                .targetValue(challenge.getTargetValue())
                .currentValue(challenge.getCurrentValue())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .status(challenge.getStatus())
                .progressRate(progressRate)
                .build();
    }
}
