package com.qurve.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StudyTimeStatisticsResponseDto {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private int todayStudyTimeMinutes;
    private int weeklyStudyTimeMinutes;
    private List<DailyStudyTimeResponseDto> dailyStudyTimes;

    public static StudyTimeStatisticsResponseDto of(
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            int todayStudyTimeMinutes,
            int weeklyStudyTimeMinutes,
            List<DailyStudyTimeResponseDto> dailyStudyTimes
    ) {
        return StudyTimeStatisticsResponseDto.builder()
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .todayStudyTimeMinutes(todayStudyTimeMinutes)
                .weeklyStudyTimeMinutes(weeklyStudyTimeMinutes)
                .dailyStudyTimes(dailyStudyTimes)
                .build();
    }
}
