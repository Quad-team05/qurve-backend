package com.qurve.vocabulary.dto.response;

import com.qurve.vocabulary.domain.UnitProgress;
import com.qurve.vocabulary.enums.UnitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitProgressResponseDto {

    private Integer unitNumber;
    private UnitStatus status;

    public static UnitProgressResponseDto from(UnitProgress unitProgress) {
        return UnitProgressResponseDto.builder()
                .unitNumber(unitProgress.getUnitNumber())
                .status(unitProgress.getStatus())
                .build();
    }
}
