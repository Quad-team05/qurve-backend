package com.qurve.vocabulary.dto.response;

import com.qurve.vocabulary.domain.VocabularyWord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UnitWordResponseDto {

    private Long wordId;
    private int orderNumber;
    private String expression;
    private String reading;
    private String meaning;
    private String koreanMeaning;

    public static UnitWordResponseDto from(VocabularyWord word, int orderNumber) {
        String displayMeaning = word.getKoreanMeaning() != null ? word.getKoreanMeaning() : word.getMeaning();

        return UnitWordResponseDto.builder()
                .wordId(word.getWordId())
                .orderNumber(orderNumber)
                .expression(word.getExpression())
                .reading(word.getReading())
                .meaning(displayMeaning)
                .koreanMeaning(word.getKoreanMeaning())
                .build();
    }
}
