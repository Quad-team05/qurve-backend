package com.qurve.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TodayLearningResponseDto {

    private String category;
    private String title;
    private Integer totalQuestionCount;
    private Integer estimatedMinutes;

    public static TodayLearningResponseDto of(
            String category,
            String title,
            Integer totalQuestionCount,
            Integer estimatedMinutes
    ) {
        return TodayLearningResponseDto.builder()
                .category(category)
                .title(title)
                .totalQuestionCount(totalQuestionCount)
                .estimatedMinutes(estimatedMinutes)
                .build();
    }
}
