package com.qurve.learning.dto.response;

import com.qurve.vocabulary.domain.UnitProgress;
import lombok.Builder;
import lombok.Getter;

/**
 * 학습 메인 카드에 표시할 현재 단어장 진행 정보입니다.
 */
@Getter
@Builder
public class CurrentVocabularyResponseDto {

    private String level;
    private Integer unitNumber;
    private String status;

    public static CurrentVocabularyResponseDto from(UnitProgress unitProgress) {
        if (unitProgress == null) {
            return null;
        }

        return CurrentVocabularyResponseDto.builder()
                .level(unitProgress.getLevel())
                .unitNumber(unitProgress.getUnitNumber())
                .status(unitProgress.getStatus().name())
                .build();
    }
}
