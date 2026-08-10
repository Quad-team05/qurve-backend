package com.qurve.xp.dto.response;

import com.qurve.global.enums.XpActionType;
import com.qurve.xp.domain.XpHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpHistoryResponseDto {

    private XpActionType actionType;
    private Integer xpAmount;
    private LocalDateTime earnedAt;

    public static XpHistoryResponseDto from(XpHistory xpHistory) {
        return XpHistoryResponseDto.builder()
                .actionType(xpHistory.getActionType())
                .xpAmount(xpHistory.getXpAmount())
                .earnedAt(xpHistory.getEarnedAt())
                .build();
    }
}
