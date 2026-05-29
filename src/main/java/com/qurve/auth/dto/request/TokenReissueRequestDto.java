package com.qurve.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenReissueRequestDto {
    @NotBlank(message = "필수로 입력해야 합니다.")
    private String refreshToken;
}
