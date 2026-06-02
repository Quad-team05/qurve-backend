package com.qurve.level.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestResultResponseDto {
    private int score;
    private int correctCount;
    private int wrongCount;
    private int level;
}
