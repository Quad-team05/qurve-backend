package com.qurve.level.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestQuestionDto {
    private int questionId;
    private String questionText;
    private String difficulty;
    private List<OptionDto> options;
    @JsonIgnore
    private int correctAnswer;
}
