package com.qurve.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TodayLearningResponseDto {

    private String level;
    private String categoryCode;
    private String subTypeCode;
    private String category;
    private String title;
    private Integer totalQuestionCount;
    private Integer estimatedMinutes;

    public static TodayLearningResponseDto of(
            String level,
            String categoryCode,
            String subTypeCode,
            String category,
            String title,
            Integer totalQuestionCount,
            Integer estimatedMinutes
    ) {
        return TodayLearningResponseDto.builder()
                .level(level)
                .categoryCode(categoryCode)
                .subTypeCode(subTypeCode)
                .category(category)
                .title(title)
                .totalQuestionCount(totalQuestionCount)
                .estimatedMinutes(estimatedMinutes)
                .build();
    }
}
