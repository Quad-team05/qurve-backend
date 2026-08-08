package com.qurve.problem.dto.response;

import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemChoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProblemSubmitResponseDto {

    private Long problemId;
    private Integer selectedChoiceNumber;
    private Integer answerChoiceNumber;
    private String answerChoiceText;
    private boolean correct;
    private String explanation;

    public static ProblemSubmitResponseDto of(
            Problem problem,
            Integer selectedChoiceNumber,
            ProblemChoice answerChoice
    ) {
        return ProblemSubmitResponseDto.builder()
                .problemId(problem.getProblemId())
                .selectedChoiceNumber(selectedChoiceNumber)
                .answerChoiceNumber(problem.getAnswerIndex())
                .answerChoiceText(answerChoice.getChoiceText())
                .correct(problem.getAnswerIndex().equals(selectedChoiceNumber))
                .explanation(problem.getExplanation())
                .build();
    }
}
