package com.qurve.problem.dto.response;

import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemChoice;
import com.qurve.problem.domain.ProblemSubmission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProblemSubmitResponseDto {

    private Long problemId;
    private Long submissionId;
    private Integer selectedChoiceNumber;
    private Integer answerChoiceNumber;
    private String answerChoiceText;
    private boolean correct;
    private String explanation;

    public static ProblemSubmitResponseDto of(
            ProblemSubmission problemSubmission,
            ProblemChoice answerChoice
    ) {
        Problem problem = problemSubmission.getProblem();

        return ProblemSubmitResponseDto.builder()
                .problemId(problem.getProblemId())
                .submissionId(problemSubmission.getSubmissionId())
                .selectedChoiceNumber(problemSubmission.getSelectedChoiceNumber())
                .answerChoiceNumber(problemSubmission.getAnswerChoiceNumber())
                .answerChoiceText(answerChoice.getChoiceText())
                .correct(problemSubmission.isCorrect())
                .explanation(problem.getExplanation())
                .build();
    }
}
