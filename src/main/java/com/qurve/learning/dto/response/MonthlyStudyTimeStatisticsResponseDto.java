package com.qurve.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MonthlyStudyTimeStatisticsResponseDto {

    private YearMonth startYearMonth;
    private YearMonth endYearMonth;
    private List<MonthlyStudyTimeResponseDto> monthlyStudyTimes;

    public static MonthlyStudyTimeStatisticsResponseDto of(
            YearMonth startYearMonth,
            YearMonth endYearMonth,
            List<MonthlyStudyTimeResponseDto> monthlyStudyTimes
    ) {
        return MonthlyStudyTimeStatisticsResponseDto.builder()
                .startYearMonth(startYearMonth)
                .endYearMonth(endYearMonth)
                .monthlyStudyTimes(monthlyStudyTimes)
                .build();
    }
}
