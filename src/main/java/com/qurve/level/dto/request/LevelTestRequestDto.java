package com.qurve.level.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestRequestDto {

    @NotNull(message = "필수로 입력해야 합니다.")
    @Min(value = 1, message = "1번 질문의 답변은 1 이상이어야 합니다.")
    @Max(value = 4, message = "1번 질문의 답변은 4 이하여야 합니다.")
    private Integer pre1Answer;

    @NotNull(message = "필수로 입력해야 합니다.")
    @Min(value = 1, message = "2번 질문의 답변은 1 이상이어야 합니다.")
    @Max(value = 3, message = "2번 질문의 답변은 3 이하여야 합니다.")
    private Integer pre2Answer;

    @NotNull(message = "필수로 입력해야 합니다.")
    @Min(value = 1, message = "3번 질문의 답변은 1 이상이어야 합니다.")
    @Max(value = 3, message = "3번 질문의 답변은 3 이하여야 합니다.")
    private Integer pre3Answer;
}
