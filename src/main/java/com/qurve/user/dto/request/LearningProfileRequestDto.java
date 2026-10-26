package com.qurve.user.dto.request;

import com.qurve.global.enums.LearningGoal;
import com.qurve.global.enums.LearningStage;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LearningProfileRequestDto {

    @NotNull(message = "학습 목적은 필수입니다.")
    private LearningGoal learningGoal;

    private LearningStage learningStage;
}