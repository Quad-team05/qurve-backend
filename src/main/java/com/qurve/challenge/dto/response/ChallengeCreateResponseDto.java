package com.qurve.challenge.dto.response;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ChallengeCreateResponseDto {

    private Long challengeId;
    private String title;
    private ChallengeGoalType goalType;
    private Integer targetValue;
    private Integer currentValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private ChallengeStatus status;

    public static ChallengeCreateResponseDto from(Challenge challenge) {
        return new ChallengeCreateResponseDto(
                challenge.getChallengeId(),
                challenge.getTitle(),
                challenge.getGoalType(),
                challenge.getTargetValue(),
                challenge.getCurrentValue(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getStatus()
        );
    }
}