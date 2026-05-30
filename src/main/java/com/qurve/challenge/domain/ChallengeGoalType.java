package com.qurve.challenge.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeGoalType {
    STUDY_TIME("학습 시간"),
    QUIZ_COUNT("퀴즈 풀이 수"),
    ATTENDANCE("출석");

    private final String description;
}