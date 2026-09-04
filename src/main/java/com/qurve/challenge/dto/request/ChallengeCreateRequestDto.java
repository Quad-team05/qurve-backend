package com.qurve.challenge.dto.request;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(description = "챌린지 생성 요청")
public class ChallengeCreateRequestDto {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "챌린지 제목", example = "단어 20개 암기")
    private String title;

    @NotNull
    @Schema(description = "목표 유형. 단어 챌린지는 WORD_COUNT를 사용합니다.", example = "WORD_COUNT")
    private ChallengeGoalType goalType;

    @NotNull
    @Positive
    @Schema(
            description = "목표값. goalType이 WORD_COUNT이면 사용자가 선택한 학습 단어 개수이며, "
                    + "챌린지 단어 조회 API가 이 수만큼 단어를 반환합니다.",
            example = "20"
    )
    private Integer targetValue;

    @NotNull
    @Schema(description = "챌린지 시작일", example = "2026-09-04")
    private LocalDate startDate;

    @NotNull
    @Schema(description = "챌린지 종료일", example = "2026-09-30")
    private LocalDate endDate;

    public Challenge toEntity(User user) {
        return Challenge.builder()
                .user(user)
                .title(title)
                .goalType(goalType)
                .targetValue(targetValue)
                .currentValue(0)
                .startDate(startDate)
                .endDate(endDate)
                .status(ChallengeStatus.ACTIVE)
                .build();
    }
}
