package com.qurve.user.dto.response;

import com.qurve.global.enums.LearningLanguage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LearningLanguageResponseDto {

    private LearningLanguage learningLanguage;

    public static LearningLanguageResponseDto of(LearningLanguage learningLanguage) {
        return LearningLanguageResponseDto.builder()
                .learningLanguage(learningLanguage)
                .build();
    }
}