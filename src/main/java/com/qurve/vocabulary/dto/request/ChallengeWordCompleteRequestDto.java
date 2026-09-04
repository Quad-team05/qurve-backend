package com.qurve.vocabulary.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

/**
 * 챌린지 단어 학습 완료 요청입니다.
 */
@Getter
public class ChallengeWordCompleteRequestDto {

    @NotEmpty
    private List<@NotNull Long> wordIds;
}
