package com.qurve.learning.dto.response;

import com.qurve.global.enums.LearningLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TodayLearningResponseDto {

    private String level;
    private LearningLanguage learningLanguage;
    private String language;
    private String cefrLevel;
    private String qurveLevel;
    private String categoryCode;
    private String subTypeCode;
    private Integer offset;
    private String category;
    private String title;
    private Integer totalQuestionCount;
    private Integer estimatedMinutes;

    public static TodayLearningResponseDto of(
            String level,
            LearningLanguage learningLanguage,
            String language,
            String cefrLevel,
            String qurveLevel,
            String categoryCode,
            String subTypeCode,
            Integer offset,
            String category,
            String title,
            Integer totalQuestionCount,
            Integer estimatedMinutes
    ) {
        return TodayLearningResponseDto.builder()
                .level(level)
                .learningLanguage(learningLanguage)
                .language(language)
                .cefrLevel(cefrLevel)
                .qurveLevel(qurveLevel)
                .categoryCode(categoryCode)
                .subTypeCode(subTypeCode)
                .offset(offset)
                .category(category)
                .title(title)
                .totalQuestionCount(totalQuestionCount)
                .estimatedMinutes(estimatedMinutes)
                .build();
    }
}
