package com.qurve.problem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProblemSubmitRequestDto {

    @NotNull(message = "selectedChoiceNumber는 필수입니다.")
    @PositiveOrZero(message = "selectedChoiceNumber는 0 이상의 값이어야 합니다.")
    private Integer selectedChoiceNumber;
}
