package com.qurve.level.dto.response;

import com.qurve.global.enums.LearningLanguage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestResponseDto {
    private LearningLanguage learningLanguage;
    private Integer caseNumber;
    private List<LevelTestQuestionDto> questions;
}