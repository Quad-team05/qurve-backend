package com.qurve.vocabulary.dto.response;

import com.qurve.vocabulary.domain.VocabularyWord;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnitWordResponseDto {

    private Long wordId;
    private int orderNumber;
    private String expression;
    private String reading;
    private String meaning;

    public static UnitWordResponseDto from(VocabularyWord word, int orderNumber) {
        return new UnitWordResponseDto(
                word.getWordId(),
                orderNumber,
                word.getExpression(),
                word.getReading(),
                word.getMeaning()
        );
    }
}