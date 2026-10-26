package com.qurve.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DailyStudyTimeResponseDto {
    private String dayOfWeek;
    private String dayLabel;
    private int studyTimeMinutes;

    public static DailyStudyTimeResponseDto of(String dayOfWeek, String dayLabel, int studyTimeMinutes) {
        return DailyStudyTimeResponseDto.builder()
                .dayOfWeek(dayOfWeek)
                .dayLabel(dayLabel)
                .studyTimeMinutes(studyTimeMinutes)
                .build();
    }
}
