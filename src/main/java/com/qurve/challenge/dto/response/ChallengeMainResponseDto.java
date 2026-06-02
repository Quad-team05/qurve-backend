package com.qurve.challenge.dto.response;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChallengeMainResponseDto {
    private Long challengeId;
    private String title;
    private ChallengeGoalType goalType;
    private Integer targetValue;
    private Integer currentValue;
    private Integer completedDays;
    private Integer progressRate;

    public static ChallengeMainResponseDto from(
            Challenge challenge,
            Integer completedDays,
            Integer progressRate
    ) {
        return ChallengeMainResponseDto.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .goalType(challenge.getGoalType())
                .targetValue(challenge.getTargetValue())
                .currentValue(challenge.getCurrentValue())
                .completedDays(completedDays)
                .progressRate(progressRate)
                .build();
    }
}
