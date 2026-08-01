package com.qurve.problem.dto.response;

import com.qurve.problem.domain.Problem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProblemResponseDto {

    private Long problemId;
    private String level;
    private String category;
    private String subType;
    private String questionFormat;
    private String questionText;
    private String passageText;
    private List<ProblemChoiceResponseDto> choices;

    public static ProblemResponseDto from(Problem problem, List<ProblemChoiceResponseDto> choices) {
        return ProblemResponseDto.builder()
                .problemId(problem.getProblemId())
                .level(problem.getLevel())
                .category(problem.getCategory())
                .subType(problem.getSubType())
                .questionFormat(problem.getQuestionFormat())
                .questionText(problem.getQuestionText())
                .passageText(problem.getPassageText())
                .choices(choices)
                .build();
    }
}
