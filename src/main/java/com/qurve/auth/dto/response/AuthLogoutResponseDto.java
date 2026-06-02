package com.qurve.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthLogoutResponseDto {

    private String message;

    public static AuthLogoutResponseDto of(String message) {
        return AuthLogoutResponseDto.builder()
                .message(message)
                .build();
    }
}