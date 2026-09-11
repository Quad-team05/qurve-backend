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
    private String qurveLevel;
    private String language;
    private String cefrLevel;
    private String usageType;
    private String category;
    private String subType;
    private String questionFormat;
    private String topic;
    private String questionText;
    private String passageText;
    private String audioUrl;
    private List<ProblemChoiceResponseDto> choices;

    public static ProblemResponseDto from(Problem problem, List<ProblemChoiceResponseDto> choices) {
        return ProblemResponseDto.builder()
                .problemId(problem.getProblemId())
                .level(problem.getLevel())
                .qurveLevel(problem.getLevel())
                .language(problem.getLanguage())
                .cefrLevel(problem.getCefrLevel())
                .usageType(problem.getUsageType())
                .category(problem.getCategory())
                .subType(problem.getSubType())
                .questionFormat(problem.getQuestionFormat())
                .topic(problem.getTopic())
                .questionText(problem.getQuestionText())
                .passageText(problem.getPassageText())
                .audioUrl(hasAudio(problem) ? "/api/problems/" + problem.getProblemId() + "/audio" : null)
                .choices(choices)
                .build();
    }

    private static boolean hasAudio(Problem problem) {
        return problem.getAudioScript() != null && !problem.getAudioScript().isBlank();
    }
}
