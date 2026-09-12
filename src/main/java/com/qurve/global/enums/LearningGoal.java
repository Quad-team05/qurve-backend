package com.qurve.global.enums;

public enum LearningGoal {
    DAILY_LIFE,
    JLPT,
    TOEIC;

    public boolean supports(LearningLanguage language) {
        return switch (language) {
            case JAPANESE -> this == DAILY_LIFE || this == JLPT;
            case ENGLISH -> this == DAILY_LIFE || this == TOEIC;
        };
    }

    public boolean requiresStageSelection() {
        return this == JLPT || this == TOEIC;
    }
}