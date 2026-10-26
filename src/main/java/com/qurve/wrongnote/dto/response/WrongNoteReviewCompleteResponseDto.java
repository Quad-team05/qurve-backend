package com.qurve.wrongnote.dto.response;

import com.qurve.wrongnote.domain.WrongNoteReview;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WrongNoteReviewCompleteResponseDto {

    private Long reviewId;
    private Integer problemCount;
    private LocalDateTime completedAt;

    public static WrongNoteReviewCompleteResponseDto from(WrongNoteReview review) {
        return WrongNoteReviewCompleteResponseDto.builder()
                .reviewId(review.getWrongNoteReviewId())
                .problemCount(review.getProblemCount())
                .completedAt(review.getCreatedAt())
                .build();
    }
}
