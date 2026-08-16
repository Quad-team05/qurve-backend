package com.qurve.learning.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudyTimeSaveRequestDto {

    @NotNull(message = "studyTimeMinutes는 필수입니다.")
    @Positive(message = "studyTimeMinutes는 1 이상의 값이어야 합니다.")
    @Max(value = 1440, message = "studyTimeMinutes는 1440 이하의 값이어야 합니다.")
    private Integer studyTimeMinutes;
}
