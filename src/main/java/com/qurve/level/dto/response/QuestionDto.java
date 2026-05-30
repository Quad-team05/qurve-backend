package com.qurve.level.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private int questionId;
    private String question;
    private List<OptionDto> options;
}
