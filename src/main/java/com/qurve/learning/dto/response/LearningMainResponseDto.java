package com.qurve.learning.dto.response;

import com.qurve.challenge.dto.response.ChallengeMainResponseDto;
import com.qurve.global.enums.LearningGoal;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.enums.LearningStage;
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

    private LearningLanguage learningLanguage;
    private LearningGoal learningGoal;
    private Integer currentLevel;
    private String currentLevelLabel;
    private LearningStage learningStage;
    private boolean learningStageEditable;
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
                .learningLanguage(user.getLearningLanguage())
                .learningGoal(user.getLearningGoal())
                .currentLevel(user.getCurrentLevel())
                .currentLevelLabel(currentLevelLabel)
                .learningStage(user.getLearningStage())
                .learningStageEditable(user.isLearningStageEditable())
                .challenges(challenges)
                .todayLearning(todayLearning)
                .wrongNoteCount(wrongNoteCount)
                .currentVocabulary(currentVocabulary)
                .bookmarkCount(bookmarkCount)
                .build();
    }
}
