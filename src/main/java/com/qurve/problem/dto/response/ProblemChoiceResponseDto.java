package com.qurve.problem.dto.response;

import com.qurve.problem.domain.ProblemChoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProblemChoiceResponseDto {

    private Integer choiceNumber;
    private String choiceText;

    public static ProblemChoiceResponseDto from(ProblemChoice problemChoice) {
        return ProblemChoiceResponseDto.builder()
                .choiceNumber(problemChoice.getChoiceNumber())
                .choiceText(problemChoice.getChoiceText())
                .build();
    }
}
