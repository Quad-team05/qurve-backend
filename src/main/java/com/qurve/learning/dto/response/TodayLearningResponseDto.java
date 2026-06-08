package com.qurve.learning.dto.response;

import com.qurve.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;

@Getter
@AllArgsConstructor
public class TodayLearningResponseDto {

    private LocalDate today;
    private String learningGoal;
    private String currentLevel;
    private Integer todayStudyTime;
    private Integer todayQuizCount;
    private Integer todayCorrectCount;

    public static TodayLearningResponseDto from(User user) {
        return new TodayLearningResponseDto(
                LocalDate.now(ZoneId.of("Asia/Seoul")),
                user.getLearningGoal(),
                user.getCurrentLevel(),
                0,
                0,
                0
        );
    }
}