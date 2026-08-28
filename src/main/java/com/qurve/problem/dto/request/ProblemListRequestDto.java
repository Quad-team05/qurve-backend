package com.qurve.problem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProblemListRequestDto {

    @NotBlank(message = "level은 필수입니다.")
    private String level;

    @NotBlank(message = "category는 필수입니다.")
    private String category;

    @NotBlank(message = "subType은 필수입니다.")
    private String subType;

    @Positive(message = "count는 1 이상의 값이어야 합니다.")
    private Integer count;

    @PositiveOrZero(message = "offset은 0 이상의 값이어야 합니다.")
    private Integer offset;
}
