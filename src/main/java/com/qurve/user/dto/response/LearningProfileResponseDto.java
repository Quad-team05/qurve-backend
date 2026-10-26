package com.qurve.user.dto.response;

import com.qurve.global.enums.LearningGoal;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.enums.LearningStage;
import com.qurve.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LearningProfileResponseDto {

    private LearningLanguage learningLanguage;
    private LearningGoal learningGoal;
    private Integer currentLevel;
    private LearningStage learningStage;
    private String learningStageLabel;
    private boolean learningStageEditable;

    public static LearningProfileResponseDto from(User user) {
        LearningStage stage = user.getLearningStage();

        return new LearningProfileResponseDto(
                user.getLearningLanguage(),
                user.getLearningGoal(),
                user.getCurrentLevel(),
                stage,
                stage == null ? null : stage.getLabel(),
                user.isLearningStageEditable()
        );
    }
}