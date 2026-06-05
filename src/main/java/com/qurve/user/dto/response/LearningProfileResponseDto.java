package com.qurve.user.dto.response;

import com.qurve.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LearningProfileResponseDto {

    private String learningGoal;
    private Integer currentLevel;

    public static LearningProfileResponseDto from(User user) {
        return new LearningProfileResponseDto(
                user.getLearningGoal(),
                user.getCurrentLevel()
        );
    }
}