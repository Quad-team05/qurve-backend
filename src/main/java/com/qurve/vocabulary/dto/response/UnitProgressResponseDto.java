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

    private String level;
    private Integer unitNumber;
    private String unitName;
    private UnitStatus status;
    private String statusText;

    public static UnitProgressResponseDto from(UnitProgress unitProgress) {
        return UnitProgressResponseDto.builder()
                .level(unitProgress.getLevel())
                .unitNumber(unitProgress.getUnitNumber())
                .unitName("UNIT " + unitProgress.getUnitNumber())
                .status(unitProgress.getStatus())
                .statusText(unitProgress.getStatus().getStatusText())
                .build();
    }

    public static UnitProgressResponseDto of(String level, Integer unitNumber, UnitStatus status) {
        return UnitProgressResponseDto.builder()
                .level(level)
                .unitNumber(unitNumber)
                .unitName("UNIT " + unitNumber)
                .status(status)
                .statusText(status.getStatusText())
                .build();
    }
}