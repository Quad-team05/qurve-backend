package com.qurve.wrongnote.dto.response;

import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemChoice;
import com.qurve.problem.domain.ProblemSubmission;
import com.qurve.problem.dto.response.ProblemChoiceResponseDto;
import com.qurve.wrongnote.domain.WrongNote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WrongNoteSolutionResponseDto {

    private Long wrongNoteId;
    private Long problemId;
    private String level;
    private String language;
    private String category;
    private String subType;
    private String questionFormat;
    private String questionText;
    private String passageText;
    private String audioUrl;
    private List<ProblemChoiceResponseDto> choices;
    private Integer selectedChoiceNumber;
    private Integer answerChoiceNumber;
    private String explanation;
    private String koreanTranslation;
    private LocalDate wrongAnsweredDate;
    private LocalDate reviewedDate;
    private boolean reviewed;
    private boolean retryCorrect;

    public static WrongNoteSolutionResponseDto of(
            WrongNote wrongNote,
            ProblemSubmission wrongSubmission,
            List<ProblemChoice> choices
    ) {
        Problem problem = wrongNote.getProblem();

        return WrongNoteSolutionResponseDto.builder()
                .wrongNoteId(wrongNote.getWrongNoteId())
                .problemId(problem.getProblemId())
                .level(problem.getLevel())
                .language(problem.resolveLanguage())
                .category(problem.getCategory())
                .subType(problem.getSubType())
                .questionFormat(problem.getQuestionFormat())
                .questionText(problem.getQuestionText())
                .passageText(problem.getPassageText())
                .audioUrl(hasAudio(problem) ? "/api/problems/" + problem.getProblemId() + "/audio" : null)
                .choices(choices.stream().map(ProblemChoiceResponseDto::from).toList())
                .selectedChoiceNumber(wrongSubmission.getSelectedChoiceNumber())
                .answerChoiceNumber(problem.getAnswerIndex())
                .explanation(problem.getExplanation())
                .koreanTranslation(problem.getKoreanTranslation())
                .wrongAnsweredDate(wrongNote.getCreatedAt().toLocalDate())
                .reviewedDate(wrongNote.getReviewedAt() == null ? null : wrongNote.getReviewedAt().toLocalDate())
                .reviewed(wrongNote.isReviewed())
                .retryCorrect(wrongNote.isRetryCorrect())
                .build();
    }

    private static boolean hasAudio(Problem problem) {
        return problem.getAudioScript() != null && !problem.getAudioScript().isBlank();
    }
}
