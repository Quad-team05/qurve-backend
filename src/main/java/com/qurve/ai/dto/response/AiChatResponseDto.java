package com.qurve.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AiChatResponseDto {

    private String message;

    public static AiChatResponseDto of(String message) {
        return AiChatResponseDto.builder()
                .message(message)
                .build();
    }
}