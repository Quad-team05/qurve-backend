package com.qurve.xp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class XpDailyResponseDto {

    private LocalDate date;
    private Integer xpAmount;

    public static XpDailyResponseDto of(LocalDate date, int xpAmount) {
        return XpDailyResponseDto.builder()
                .date(date)
                .xpAmount(xpAmount)
                .build();
    }
}