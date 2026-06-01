package com.qurve.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AttendanceResponseDto {
    private int streakDays;
    private boolean checkedToday;
    private List<AttendanceDayResponseDto> days;

    public static AttendanceResponseDto from(
            int streakDays,
            boolean checkedToday,
            List<AttendanceDayResponseDto> days
    ) {
        return AttendanceResponseDto.builder()
                .streakDays(streakDays)
                .checkedToday(checkedToday)
                .days(days)
                .build();
    }
}
