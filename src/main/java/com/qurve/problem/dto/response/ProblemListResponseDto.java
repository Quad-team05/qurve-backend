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
    private String qurveLevel;
    private String language;
    private String cefrLevel;
    private String usageType;
    private String category;
    private String subType;
    private String topic;
    private Integer totalProblemCount;
    private Integer offset;
    private Integer problemCount;
    private List<ProblemResponseDto> problems;

    public static ProblemListResponseDto of(
            String level,
            String language,
            String cefrLevel,
            String usageType,
            String category,
            String subType,
            String topic,
            Integer totalProblemCount,
            Integer offset,
            List<ProblemResponseDto> problems
    ) {
        return ProblemListResponseDto.builder()
                .level(level)
                .qurveLevel(level)
                .language(language)
                .cefrLevel(cefrLevel)
                .usageType(usageType)
                .category(category)
                .subType(subType)
                .topic(topic)
                .totalProblemCount(totalProblemCount)
                .offset(offset)
                .problemCount(problems.size())
                .problems(problems)
                .build();
    }
}
