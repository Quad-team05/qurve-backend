package com.qurve.user.dto.request;

import com.qurve.global.enums.LearningLanguage;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LearningLanguageRequestDto {

    @NotNull
    private LearningLanguage learningLanguage;
}