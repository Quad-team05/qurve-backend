package com.qurve.challenge.dto.response;

import com.qurve.challenge.domain.ChallengeGoalType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChallengeGoalTypeResponseDto {
    private String code;
    private String description;

    public static ChallengeGoalTypeResponseDto from(ChallengeGoalType type) {
        return new ChallengeGoalTypeResponseDto(
                type.name(),
                type.getDescription()
        );
    }
}