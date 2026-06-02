package com.qurve.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AttendanceDayResponseDto {
    private String dayOfWeek;
    private boolean checked;

    public static AttendanceDayResponseDto of(String dayOfWeek, boolean checked) {
        return AttendanceDayResponseDto.builder()
                .dayOfWeek(dayOfWeek)
                .checked(checked)
                .build();
    }
}
