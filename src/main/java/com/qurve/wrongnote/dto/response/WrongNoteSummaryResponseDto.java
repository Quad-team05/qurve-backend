package com.qurve.wrongnote.dto.response;

import com.qurve.problem.domain.Problem;
import com.qurve.wrongnote.domain.WrongNote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class WrongNoteSummaryResponseDto {

    private Long wrongNoteId;
    private Long problemId;
    private String title;
    private String level;
    private String category;
    private String subType;
    private LocalDate wrongAnsweredDate;
    private LocalDate reviewedDate;
    private boolean reviewed;
    private boolean retryCorrect;

    public static WrongNoteSummaryResponseDto from(WrongNote wrongNote, String title) {
        Problem problem = wrongNote.getProblem();

        return WrongNoteSummaryResponseDto.builder()
                .wrongNoteId(wrongNote.getWrongNoteId())
                .problemId(problem.getProblemId())
                .title(title)
                .level(problem.getLevel())
                .category(problem.getCategory())
                .subType(problem.getSubType())
                .wrongAnsweredDate(wrongNote.getCreatedAt().toLocalDate())
                .reviewedDate(wrongNote.getReviewedAt() == null ? null : wrongNote.getReviewedAt().toLocalDate())
                .reviewed(wrongNote.isReviewed())
                .retryCorrect(wrongNote.isRetryCorrect())
                .build();
    }
}
