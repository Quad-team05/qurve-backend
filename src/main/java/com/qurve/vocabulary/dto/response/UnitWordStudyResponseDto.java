package com.qurve.vocabulary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UnitWordStudyResponseDto {

    private String level;
    private Integer unitNumber;
    private int totalCount;
    private List<UnitWordResponseDto> words;

    public static UnitWordStudyResponseDto of(
            String level,
            Integer unitNumber,
            List<UnitWordResponseDto> words
    ) {
        return new UnitWordStudyResponseDto(
                level,
                unitNumber,
                words.size(),
                words
        );
    }
}