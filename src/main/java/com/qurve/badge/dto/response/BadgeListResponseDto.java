package com.qurve.badge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BadgeListResponseDto {

    private int totalCount;
    private int achievedCount;
    private List<BadgeResponseDto> badges;

    public static BadgeListResponseDto from(List<BadgeResponseDto> badges) {
        return BadgeListResponseDto.builder()
                .totalCount(badges.size())
                .achievedCount((int) badges.stream()
                        .filter(BadgeResponseDto::isAchieved)
                        .count())
                .badges(badges)
                .build();
    }
}
