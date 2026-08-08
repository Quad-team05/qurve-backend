package com.qurve.badge.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum BadgeDefinition {
    FIRST_LOGIN("첫 발걸음", "🐣", BadgeCategory.ATTENDANCE, "첫 로그인", BadgeConditionType.FIRST_LOGIN, 1),
    ATTENDANCE_STREAK_3("3일 개근", "🐥", BadgeCategory.ATTENDANCE, "3일 연속 출석", BadgeConditionType.ATTENDANCE_STREAK, 3),
    ATTENDANCE_STREAK_7("7일 개근", "🐓", BadgeCategory.ATTENDANCE, "7일 연속 출석", BadgeConditionType.ATTENDANCE_STREAK, 7),
    ATTENDANCE_STREAK_30("30일 개근", "🦅", BadgeCategory.ATTENDANCE, "30일 연속 출석", BadgeConditionType.ATTENDANCE_STREAK, 30),
    ATTENDANCE_STREAK_100("100일 개근", "🦁", BadgeCategory.ATTENDANCE, "100일 연속 출석", BadgeConditionType.ATTENDANCE_STREAK, 100),
    ATTENDANCE_STREAK_365("365일 개근", "🐉", BadgeCategory.ATTENDANCE, "365일 연속 출석", BadgeConditionType.ATTENDANCE_STREAK, 365),

    FIRST_LEARNING("첫 학습", "🌱", BadgeCategory.LEARNING, "첫 문제 제출", BadgeConditionType.PROBLEM_SUBMISSION_COUNT, 1),
    LEARNING_10("새싹 학습자", "🌿", BadgeCategory.LEARNING, "문제 10회 제출", BadgeConditionType.PROBLEM_SUBMISSION_COUNT, 10),
    LEARNING_50("성실한 학습자", "🌳", BadgeCategory.LEARNING, "문제 50회 제출", BadgeConditionType.PROBLEM_SUBMISSION_COUNT, 50),
    LEARNING_100("학습 고수", "🌲", BadgeCategory.LEARNING, "문제 100회 제출", BadgeConditionType.PROBLEM_SUBMISSION_COUNT, 100),
    LEARNING_500("학습 전설", "🎄", BadgeCategory.LEARNING, "문제 500회 제출", BadgeConditionType.PROBLEM_SUBMISSION_COUNT, 500),

    FIRST_CORRECT("첫 정답", "🎯", BadgeCategory.ACCURACY, "첫 문제 정답", BadgeConditionType.CORRECT_PROBLEM_COUNT, 1),
    ACCURACY_50("절반은 맞춰", "🎪", BadgeCategory.ACCURACY, "정답률 50% 달성", BadgeConditionType.ACCURACY_RATE, 50),
    ACCURACY_80("우등생", "🎓", BadgeCategory.ACCURACY, "정답률 80% 달성", BadgeConditionType.ACCURACY_RATE, 80),
    PERFECT_SET("만점왕", "👑", BadgeCategory.ACCURACY, "한 세트 100점 달성", BadgeConditionType.PERFECT_SET, 1),
    CONSECUTIVE_PERFECT_SET_3("연속 만점", "💥", BadgeCategory.ACCURACY, "3세트 연속 100점 달성", BadgeConditionType.CONSECUTIVE_PERFECT_SET, 3),

    FIRST_WRONG_NOTE_REVIEW("첫 복습", "🔍", BadgeCategory.WRONG_NOTE, "오답노트 첫 학습", BadgeConditionType.WRONG_NOTE_STUDY_COUNT, 1),
    WRONG_NOTE_REVIEW_10("복습 습관", "🔎", BadgeCategory.WRONG_NOTE, "오답노트 10회 학습", BadgeConditionType.WRONG_NOTE_STUDY_COUNT, 10),
    WRONG_RETRY_CORRECT_10("오답 극복", "🦾", BadgeCategory.WRONG_NOTE, "틀린 문제 복습 후 정답 10회", BadgeConditionType.WRONG_RETRY_CORRECT_COUNT, 10),
    WRONG_RETRY_CORRECT_100("오답 마스터", "🧩", BadgeCategory.WRONG_NOTE, "틀린 문제 복습 후 정답 100회", BadgeConditionType.WRONG_RETRY_CORRECT_COUNT, 100),

    WORD_STUDY_10("단어 입문", "🌰", BadgeCategory.VOCABULARY, "단어 10개 학습", BadgeConditionType.WORD_STUDY_COUNT, 10),
    WORD_STUDY_50("단어 수집가", "🍀", BadgeCategory.VOCABULARY, "단어 50개 학습", BadgeConditionType.WORD_STUDY_COUNT, 50),
    WORD_STUDY_100("단어 마스터", "🌺", BadgeCategory.VOCABULARY, "단어 100개 학습", BadgeConditionType.WORD_STUDY_COUNT, 100),
    WORD_STUDY_500("단어 박사", "🌸", BadgeCategory.VOCABULARY, "단어 500개 학습", BadgeConditionType.WORD_STUDY_COUNT, 500),
    WORD_STUDY_1000("단어 전설", "🌻", BadgeCategory.VOCABULARY, "단어 1000개 학습", BadgeConditionType.WORD_STUDY_COUNT, 1000),
    FIRST_WORD_BOOKMARK("북마크 시작", "🔖", BadgeCategory.VOCABULARY, "단어 북마크 첫 등록", BadgeConditionType.WORD_BOOKMARK_COUNT, 1),
    WORD_BOOKMARK_50("북마크 수집가", "📌", BadgeCategory.VOCABULARY, "단어 북마크 50개 등록", BadgeConditionType.WORD_BOOKMARK_COUNT, 50),

    FIRST_CHALLENGE("첫 도전", "🏁", BadgeCategory.CHALLENGE, "챌린지 첫 등록", BadgeConditionType.CHALLENGE_CREATED_COUNT, 1),
    CHALLENGE_COMPLETED_1("도전 완료", "🥉", BadgeCategory.CHALLENGE, "챌린지 1개 달성", BadgeConditionType.CHALLENGE_COMPLETED_COUNT, 1),
    CHALLENGE_COMPLETED_3("도전 중급", "🥈", BadgeCategory.CHALLENGE, "챌린지 3개 달성", BadgeConditionType.CHALLENGE_COMPLETED_COUNT, 3),
    CHALLENGE_COMPLETED_5("도전 고수", "🥇", BadgeCategory.CHALLENGE, "챌린지 5개 달성", BadgeConditionType.CHALLENGE_COMPLETED_COUNT, 5),
    CHALLENGE_COMPLETED_10("챌린지 왕", "🏆", BadgeCategory.CHALLENGE, "챌린지 10개 달성", BadgeConditionType.CHALLENGE_COMPLETED_COUNT, 10),
    CHALLENGE_COMPLETED_30("챌린지 전설", "🎖️", BadgeCategory.CHALLENGE, "챌린지 30개 달성", BadgeConditionType.CHALLENGE_COMPLETED_COUNT, 30),

    STUDY_TIME_60("첫 1시간", "⏰", BadgeCategory.STUDY_TIME, "누적 학습 1시간", BadgeConditionType.TOTAL_STUDY_TIME, 60),
    STUDY_TIME_600("10시간 돌파", "🕙", BadgeCategory.STUDY_TIME, "누적 학습 10시간", BadgeConditionType.TOTAL_STUDY_TIME, 600),
    STUDY_TIME_3000("50시간 돌파", "🕔", BadgeCategory.STUDY_TIME, "누적 학습 50시간", BadgeConditionType.TOTAL_STUDY_TIME, 3000),
    STUDY_TIME_6000("100시간 돌파", "🕐", BadgeCategory.STUDY_TIME, "누적 학습 100시간", BadgeConditionType.TOTAL_STUDY_TIME, 6000),
    STUDY_TIME_30000("500시간 돌파", "⌚", BadgeCategory.STUDY_TIME, "누적 학습 500시간", BadgeConditionType.TOTAL_STUDY_TIME, 30000),

    FIRST_LEVEL_UP("첫 레벨업", "🚀", BadgeCategory.LEVEL, "첫 레벨업 달성", BadgeConditionType.LEVEL_UP_COUNT, 1),
    N5_MASTER("N5 마스터", "🌱", BadgeCategory.LEVEL, "N5 문제 정답률 80% 이상 + 50세트 완료", BadgeConditionType.LEVEL_MASTER, 5),
    N4_MASTER("N4 마스터", "🌿", BadgeCategory.LEVEL, "N4 문제 정답률 80% 이상 + 50세트 완료", BadgeConditionType.LEVEL_MASTER, 4),
    N3_MASTER("N3 마스터", "🌳", BadgeCategory.LEVEL, "N3 문제 정답률 80% 이상 + 50세트 완료", BadgeConditionType.LEVEL_MASTER, 3),
    N2_MASTER("N2 마스터", "🌲", BadgeCategory.LEVEL, "N2 문제 정답률 80% 이상 + 50세트 완료", BadgeConditionType.LEVEL_MASTER, 2),
    N1_MASTER("N1 마스터", "🎋", BadgeCategory.LEVEL, "N1 문제 정답률 80% 이상 + 50세트 완료", BadgeConditionType.LEVEL_MASTER, 1),
    JLPT_CONQUEROR("JLPT 정복자", "🎌", BadgeCategory.LEVEL, "N1~N5 마스터까지 전부 달성", BadgeConditionType.ALL_LEVEL_MASTER, 5),

    FIRST_AI_CHAT("AI 첫 대화", "💬", BadgeCategory.AI, "AI 학습 코치 첫 사용", BadgeConditionType.AI_COACH_USAGE_COUNT, 1),
    AI_CHAT_10("AI 단골", "🗣️", BadgeCategory.AI, "AI 학습 코치 10회 사용", BadgeConditionType.AI_COACH_USAGE_COUNT, 10),
    AI_CHAT_50("AI 친구", "🤝", BadgeCategory.AI, "AI 학습 코치 50회 사용", BadgeConditionType.AI_COACH_USAGE_COUNT, 50);

    private final String name;
    private final String emoji;
    private final BadgeCategory category;
    private final String description;
    private final BadgeConditionType conditionType;
    private final int targetValue;

    public static List<BadgeDefinition> orderedValues() {
        return Arrays.asList(values());
    }
}
