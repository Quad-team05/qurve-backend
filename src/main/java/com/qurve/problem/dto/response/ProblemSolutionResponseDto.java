package com.qurve.problem.dto.response;

import com.qurve.problem.domain.ProblemChoice;
import com.qurve.problem.domain.ProblemSubmission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProblemSolutionResponseDto {

    private Long submissionId;
    private Integer selectedChoiceNumber;
    private Integer answerChoiceNumber;
    private String answerChoiceText;
    private boolean correct;
    private String explanation;
    private LocalDateTime submittedAt;

    public static ProblemSolutionResponseDto of(
            ProblemSubmission problemSubmission,
            ProblemChoice answerChoice
    ) {
        return ProblemSolutionResponseDto.builder()
                .submissionId(problemSubmission.getSubmissionId())
                .selectedChoiceNumber(problemSubmission.getSelectedChoiceNumber())
                .answerChoiceNumber(problemSubmission.getAnswerChoiceNumber())
                .answerChoiceText(answerChoice.getChoiceText())
                .correct(problemSubmission.isCorrect())
                .explanation(problemSubmission.getProblem().getExplanation())
                .submittedAt(problemSubmission.getCreatedAt())
                .build();
    }
}
