package com.qurve.level.dto.request;

import com.qurve.global.enums.LearningLanguage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveLevelRequestDto {

    @NotNull
    private LearningLanguage learningLanguage;

    @NotNull
    private Integer level;
}
