package com.qurve.level.dto.response;

import com.qurve.global.enums.LearningLanguage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestResultResponseDto {
    private LearningLanguage learningLanguage;
    private int caseNumber;
    private int score;
    private int correctCount;
    private int wrongCount;
    private int level;
    private List<LevelTestQuestionResultResponseDto> questionResults;
}
