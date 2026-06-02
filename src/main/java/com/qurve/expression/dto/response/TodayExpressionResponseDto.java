package com.qurve.expression.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TodayExpressionResponseDto {

    private Long sentenceId;
    private String japanese;
    private String korean;
    private String sourceUrl;
    private String license;

    public static TodayExpressionResponseDto of(
            Long sentenceId,
            String japanese,
            String korean,
            String sourceUrl,
            String license
    ) {
        return TodayExpressionResponseDto.builder()
                .sentenceId(sentenceId)
                .japanese(japanese)
                .korean(korean)
                .sourceUrl(sourceUrl)
                .license(license)
                .build();
    }
}
