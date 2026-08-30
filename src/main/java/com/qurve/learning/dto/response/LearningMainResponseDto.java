package com.qurve.learning.dto.response;

import com.qurve.challenge.dto.response.ChallengeMainResponseDto;
import com.qurve.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 학습 메인 화면에 필요한 사용자 학습 정보를 한 번에 반환합니다.
 */
@Getter
@Builder
public class LearningMainResponseDto {

    private String learningGoal;
    private Integer currentLevel;
    private String currentLevelLabel;
    private List<ChallengeMainResponseDto> challenges;
    private TodayLearningResponseDto todayLearning;
    private Long wrongNoteCount;
    private CurrentVocabularyResponseDto currentVocabulary;
    private Long bookmarkCount;

    public static LearningMainResponseDto of(
            User user,
            String currentLevelLabel,
            List<ChallengeMainResponseDto> challenges,
            TodayLearningResponseDto todayLearning,
            long wrongNoteCount,
            CurrentVocabularyResponseDto currentVocabulary,
            long bookmarkCount
    ) {
        return LearningMainResponseDto.builder()
                .learningGoal(user.getLearningGoal())
                .currentLevel(user.getCurrentLevel())
                .currentLevelLabel(currentLevelLabel)
                .challenges(challenges)
                .todayLearning(todayLearning)
                .wrongNoteCount(wrongNoteCount)
                .currentVocabulary(currentVocabulary)
                .bookmarkCount(bookmarkCount)
                .build();
    }
}
