package com.qurve.level.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestResultRequestDto {

    @NotNull(message = "필수로 입력해야 합니다.")
    private Integer pre1Answer;

    @NotNull(message = "필수로 입력해야 합니다.")
    private Integer pre2Answer;

    @NotNull(message = "필수로 입력해야 합니다.")
    private Integer pre3Answer;

    @NotNull(message = "필수로 입력해야 합니다.")
    @Size(min = 10, max = 10, message = "답안은 10개여야 합니다.")
    private List<Integer> answers;
}
