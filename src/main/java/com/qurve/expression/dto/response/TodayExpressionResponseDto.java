package com.qurve.expression.dto.response;

import com.qurve.global.enums.LearningLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TodayExpressionResponseDto {

    private Long sentenceId;
    private String expression;
    private LearningLanguage learningLanguage;

    /**
     * 기존 일본어 오늘의 표현 화면과의 응답 호환을 위한 필드입니다.
     * 학습 언어와 관계없이 expression과 같은 값을 반환합니다.
     */
    private String japanese;
    private String korean;
    private String sourceUrl;
    private String license;

    public static TodayExpressionResponseDto of(
            Long sentenceId,
            String japanese,
            String korean,
            String sourceUrl,
            String license,
            LearningLanguage learningLanguage
    ) {
        return TodayExpressionResponseDto.builder()
                .sentenceId(sentenceId)
                .expression(japanese)
                .learningLanguage(learningLanguage)
                .japanese(japanese)
                .korean(korean)
                .sourceUrl(sourceUrl)
                .license(license)
                .build();
    }
}
