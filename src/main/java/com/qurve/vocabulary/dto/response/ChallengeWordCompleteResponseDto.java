package com.qurve.vocabulary.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 챌린지 단어 학습 완료 결과입니다.
 */
@Getter
@Builder
public class ChallengeWordCompleteResponseDto {

    private Integer submittedWordCount;
    private Integer newlyLearnedWordCount;

    public static ChallengeWordCompleteResponseDto of(int submittedWordCount, int newlyLearnedWordCount) {
        return ChallengeWordCompleteResponseDto.builder()
                .submittedWordCount(submittedWordCount)
                .newlyLearnedWordCount(newlyLearnedWordCount)
                .build();
    }
}
