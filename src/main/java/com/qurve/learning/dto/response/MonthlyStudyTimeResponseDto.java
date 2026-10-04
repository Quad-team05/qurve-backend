package com.qurve.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@Builder
@AllArgsConstructor
public class MonthlyStudyTimeResponseDto {

    private YearMonth yearMonth;
    private int studyTimeMinutes;

    public static MonthlyStudyTimeResponseDto of(YearMonth yearMonth, int studyTimeMinutes) {
        return MonthlyStudyTimeResponseDto.builder()
                .yearMonth(yearMonth)
                .studyTimeMinutes(studyTimeMinutes)
                .build();
    }
}
