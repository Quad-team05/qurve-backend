package com.qurve.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LearningProfileRequestDto {

    @NotBlank
    @Size(max = 255)
    private String learningGoal;

    @NotNull
    @Positive
    private Integer currentLevel;
}
