package com.qurve.problem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProblemAccuracyTrendResponseDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyProblemAccuracyResponseDto> dailyAccuracies;

    public static ProblemAccuracyTrendResponseDto of(
            LocalDate startDate,
            LocalDate endDate,
            List<DailyProblemAccuracyResponseDto> dailyAccuracies
    ) {
        return ProblemAccuracyTrendResponseDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dailyAccuracies(dailyAccuracies)
                .build();
    }
}
