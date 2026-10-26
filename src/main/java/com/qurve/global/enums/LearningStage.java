package com.qurve.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningStage {

    JLPT_N1(LearningLanguage.JAPANESE, LearningGoal.JLPT, "N1"),
    JLPT_N2(LearningLanguage.JAPANESE, LearningGoal.JLPT, "N2"),
    JLPT_N3(LearningLanguage.JAPANESE, LearningGoal.JLPT, "N3"),
    JLPT_N4(LearningLanguage.JAPANESE, LearningGoal.JLPT, "N4"),
    JLPT_N5(LearningLanguage.JAPANESE, LearningGoal.JLPT, "N5"),

    TOEIC_500_PLUS(LearningLanguage.ENGLISH, LearningGoal.TOEIC, "500+"),
    TOEIC_600_PLUS(LearningLanguage.ENGLISH, LearningGoal.TOEIC, "600+"),
    TOEIC_700_PLUS(LearningLanguage.ENGLISH, LearningGoal.TOEIC, "700+"),
    TOEIC_800_PLUS(LearningLanguage.ENGLISH, LearningGoal.TOEIC, "800+"),
    TOEIC_900_PLUS(LearningLanguage.ENGLISH, LearningGoal.TOEIC, "900+");

    private final LearningLanguage language;
    private final LearningGoal goal;
    private final String label;

    public boolean matches(LearningLanguage learningLanguage, LearningGoal learningGoal) {
        return language == learningLanguage && goal == learningGoal;
    }
}