package com.qurve.problem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProblemListResponseDto {

    private String level;
    private String category;
    private String subType;
    private Integer totalProblemCount;
    private Integer offset;
    private Integer problemCount;
    private List<ProblemResponseDto> problems;

    public static ProblemListResponseDto of(
            String level,
            String category,
            String subType,
            Integer totalProblemCount,
            Integer offset,
            List<ProblemResponseDto> problems
    ) {
        return ProblemListResponseDto.builder()
                .level(level)
                .category(category)
                .subType(subType)
                .totalProblemCount(totalProblemCount)
                .offset(offset)
                .problemCount(problems.size())
                .problems(problems)
                .build();
    }
}
