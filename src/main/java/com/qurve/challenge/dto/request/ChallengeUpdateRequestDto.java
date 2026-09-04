package com.qurve.challenge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 진행 중인 챌린지 수정 요청입니다.
 */
@Getter
public class ChallengeUpdateRequestDto {

    @NotBlank
    @Size(max = 50)
    private String title;

    @NotNull
    @Positive
    private Integer targetValue;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
