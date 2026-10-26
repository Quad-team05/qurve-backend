package com.qurve.problem.dto.response;

import com.qurve.problem.domain.ProblemSetCompletion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProblemSetCompleteResponseDto {

    private Long completionId;
    private Integer problemCount;
    private Integer correctCount;
    private boolean perfect;
    private LocalDateTime completedAt;

    public static ProblemSetCompleteResponseDto from(ProblemSetCompletion completion) {
        return ProblemSetCompleteResponseDto.builder()
                .completionId(completion.getProblemSetCompletionId())
                .problemCount(completion.getProblemCount())
                .correctCount(completion.getCorrectCount())
                .perfect(completion.isPerfect())
                .completedAt(completion.getCreatedAt())
                .build();
    }
}
