package com.qurve.level.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestRequestDto {

    @NotNull(message = "필수로 입력해야 합니다.")
    private int pre1Answer;

    @NotNull(message = "필수로 입력해야 합니다.")
    private int pre2Answer;

    @NotNull(message = "필수로 입력해야 합니다.")
    private int pre3Answer;
}
