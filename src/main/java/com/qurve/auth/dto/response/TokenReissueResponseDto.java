package com.qurve.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenReissueResponseDto {
    private String accessToken;
    private String refreshToken;

    public static TokenReissueResponseDto from(String accessToken, String refreshToken) {
        return TokenReissueResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
