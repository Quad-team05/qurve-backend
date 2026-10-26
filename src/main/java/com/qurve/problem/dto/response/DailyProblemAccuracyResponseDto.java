package com.qurve.problem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class DailyProblemAccuracyResponseDto {
    private LocalDate date;
    private String dayOfWeek;
    private String dayLabel;
    private int totalSubmissionCount;
    private int correctSubmissionCount;
    private int wrongSubmissionCount;
    private int accuracyRate;

    public static DailyProblemAccuracyResponseDto of(
            LocalDate date,
            String dayOfWeek,
            String dayLabel,
            int totalSubmissionCount,
            int correctSubmissionCount
    ) {
        int accuracyRate = totalSubmissionCount == 0
                ? 0
                : (int) Math.floor(correctSubmissionCount * 100.0 / totalSubmissionCount);

        return DailyProblemAccuracyResponseDto.builder()
                .date(date)
                .dayOfWeek(dayOfWeek)
                .dayLabel(dayLabel)
                .totalSubmissionCount(totalSubmissionCount)
                .correctSubmissionCount(correctSubmissionCount)
                .wrongSubmissionCount(totalSubmissionCount - correctSubmissionCount)
                .accuracyRate(accuracyRate)
                .build();
    }
}
