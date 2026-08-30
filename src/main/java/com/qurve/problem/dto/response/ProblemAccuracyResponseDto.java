package com.qurve.problem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProblemAccuracyResponseDto {
    private int totalSubmissionCount;
    private int correctSubmissionCount;
    private int wrongSubmissionCount;
    private int accuracyRate;

    public static ProblemAccuracyResponseDto of(
            int totalSubmissionCount,
            int correctSubmissionCount
    ) {
        int accuracyRate = totalSubmissionCount == 0
                ? 0
                : (int) Math.floor(correctSubmissionCount * 100.0 / totalSubmissionCount);

        return ProblemAccuracyResponseDto.builder()
                .totalSubmissionCount(totalSubmissionCount)
                .correctSubmissionCount(correctSubmissionCount)
                .wrongSubmissionCount(totalSubmissionCount - correctSubmissionCount)
                .accuracyRate(accuracyRate)
                .build();
    }
}
