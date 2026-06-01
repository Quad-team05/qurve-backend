package com.qurve.level.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveLevelRequestDto {

    @NotNull(message = "필수로 입력해야 합니다.")
    private Integer level;
}
