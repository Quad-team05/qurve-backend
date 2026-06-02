package com.qurve.learning.dto.response;

import com.qurve.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LearningMainResponseDto {

    private String nickname;
    private String learningGoal;
    private String currentLevel;

    public static LearningMainResponseDto from(User user) {
        return new LearningMainResponseDto(
                user.getNickname(),
                user.getLearningGoal(),
                user.getCurrentLevel()
        );
    }
}