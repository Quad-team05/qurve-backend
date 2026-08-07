package com.qurve.badge.dto.response;

import com.qurve.badge.domain.BadgeCategory;
import com.qurve.badge.domain.BadgeDefinition;
import com.qurve.badge.domain.UserBadge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BadgeResponseDto {

    private String code;
    private String name;
    private String emoji;
    private BadgeCategory category;
    private String description;
    private boolean achieved;
    private LocalDateTime achievedAt;
    private int currentValue;
    private int targetValue;
    private int progressRate;

    public static BadgeResponseDto of(
            BadgeDefinition badgeDefinition,
            UserBadge userBadge,
            int currentValue
    ) {
        int targetValue = badgeDefinition.getTargetValue();
        int progressRate = targetValue <= 0 ? 0 : Math.min((int) Math.floor(currentValue * 100.0 / targetValue), 100);

        return BadgeResponseDto.builder()
                .code(badgeDefinition.name())
                .name(badgeDefinition.getName())
                .emoji(badgeDefinition.getEmoji())
                .category(badgeDefinition.getCategory())
                .description(badgeDefinition.getDescription())
                .achieved(userBadge != null)
                .achievedAt(userBadge == null ? null : userBadge.getAchievedAt())
                .currentValue(currentValue)
                .targetValue(targetValue)
                .progressRate(progressRate)
                .build();
    }
}
