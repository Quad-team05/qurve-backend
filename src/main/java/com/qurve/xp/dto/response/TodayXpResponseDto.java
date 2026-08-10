package com.qurve.xp.dto.response;

import com.qurve.xp.domain.XpHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayXpResponseDto {

    private Integer totalXp;
    private List<XpHistoryResponseDto> histories;

    public static TodayXpResponseDto of(List<XpHistory> histories) {

        List<XpHistoryResponseDto> historyDtos = histories.stream()
                .map(XpHistoryResponseDto::from)
                .toList();

        int totalXp = historyDtos.stream()
                .mapToInt(XpHistoryResponseDto::getXpAmount)
                .sum();

        return TodayXpResponseDto.builder()
                .totalXp(totalXp)
                .histories(historyDtos)
                .build();
    }
}
