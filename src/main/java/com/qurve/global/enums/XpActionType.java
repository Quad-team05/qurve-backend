package com.qurve.global.enums;

public enum XpActionType {
    DAILY_ATTENDANCE,        // 일일 출석 +10
    STREAK_3_DAYS,           // 3일 연속 출석 보너스 +30
    STREAK_7_DAYS,           // 7일 연속 출석 보너스 +70
    PROBLEM_CORRECT,         // 문제 1개 정답 +10
    PROBLEM_SET_COMPLETE,    // 문제 1세트 완료 +30
    PROBLEM_SET_PERFECT,     // 문제 1세트 100점 +50
    WRONG_NOTE_COMPLETE,     // 오답노트 복습 완료 +15
    WRONG_NOTE_CORRECT,      // 오답 복습 후 정답 +20
    WORD_LEARN,              // 단어 1개 학습 +5
    WORD_SET_COMPLETE,       // 단어 학습 1세트 완료 +20
    WORD_BOOKMARK,           // 단어 북마크 등록 +3
    CHALLENGE_COMPLETE,      // 챌린지 달성 +100
    DAILY_GOAL_COMPLETE,     // 일일 목표 달성 +50
    AI_COACH_FIRST,          // AI 학습 코치 첫 사용 +20
    AI_COACH_DAILY           // AI 학습 코치 일일 사용 +5
}
