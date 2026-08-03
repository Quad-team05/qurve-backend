package com.qurve.problem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProblemSolutionListResponseDto {

    private Long problemId;
    private List<ProblemSolutionResponseDto> solutions;

    public static ProblemSolutionListResponseDto of(
            Long problemId,
            List<ProblemSolutionResponseDto> solutions
    ) {
        return ProblemSolutionListResponseDto.builder()
                .problemId(problemId)
                .solutions(solutions)
                .build();
    }
}
