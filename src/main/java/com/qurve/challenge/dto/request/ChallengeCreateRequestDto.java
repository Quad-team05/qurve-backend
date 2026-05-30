package com.qurve.challenge.dto.request;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.user.domain.User;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ChallengeCreateRequestDto {

    @NotBlank
    @Size(max = 50)
    private String title;

    @NotNull
    private ChallengeGoalType goalType;

    @NotNull
    @Positive
    private Integer targetValue;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    public Challenge toEntity(User user) {
        return Challenge.builder()
                .user(user)
                .title(title)
                .goalType(goalType)
                .targetValue(targetValue)
                .currentValue(0)
                .startDate(startDate)
                .endDate(endDate)
                .status(ChallengeStatus.ACTIVE)
                .build();
    }
}