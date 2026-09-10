package com.qurve.level.dto.request;

import com.qurve.global.enums.LearningLanguage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestResultRequestDto {

    @NotNull
    private LearningLanguage learningLanguage;

    @NotNull
    @Min(1)
    @Max(3)
    private Integer caseNumber;

    @NotNull
    @Size(min = 10, max = 10)
    private List<Integer> answers;
}
