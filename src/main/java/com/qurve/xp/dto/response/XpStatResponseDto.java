package com.qurve.xp.dto.response;

import com.qurve.xp.enums.LevelDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpStatResponseDto {

    private Integer currentLevel;
    private String title;
    private Integer totalXp;
    private Integer currentLevelXp;
    private Integer nextLevelXp;
    private Integer xpToNextLevel;
    private Integer streakDays;

    public static XpStatResponseDto of(int totalXp, int streakDays) {

        LevelDefinition current = LevelDefinition.fromXp(totalXp);
        LevelDefinition next = current.next();

        return XpStatResponseDto.builder()
                .currentLevel(current.getLevel())
                .title(current.getTitle())
                .totalXp(totalXp)
                .currentLevelXp(current.getRequiredXp())
                .xpToNextLevel(current.isMaxLevel() ? 0 : next.getRequiredXp() - totalXp)
                .nextLevelXp(current.isMaxLevel() ? current.getRequiredXp() : next.getRequiredXp())
                .streakDays(streakDays)
                .build();
    }
}
