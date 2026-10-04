package com.qurve.expression.dto.response;

import com.qurve.expression.domain.BasicExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BasicExpressionResponseDto {

    private Long expressionId;
    private Integer orderNumber;
    private String category;
    private String kanji;
    private String hiragana;
    private String romaji;
    private String translation;

    public static BasicExpressionResponseDto from(BasicExpression expression) {
        return BasicExpressionResponseDto.builder()
                .expressionId(expression.getExpressionId())
                .orderNumber(expression.getOrderNumber())
                .category(expression.getCategory())
                .kanji(expression.getKanji())
                .hiragana(expression.getHiragana())
                .romaji(expression.getRomaji())
                .translation(expression.getTranslation())
                .build();
    }
}