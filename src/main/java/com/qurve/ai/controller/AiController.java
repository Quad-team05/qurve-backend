package com.qurve.ai.controller;

import com.qurve.ai.dto.request.AiChatRequestDto;
import com.qurve.ai.dto.response.AiChatResponseDto;
import com.qurve.ai.service.AiService;
import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI 언어 학습 대화 기능 API")
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @Operation(summary = "AI 대화 전송", description = "현재 학습 언어의 AI 튜터에게 메시지를 전송하고 응답을 반환합니다.")
    public ResponseEntity<ApiResponse<AiChatResponseDto>> chat(@Valid @RequestBody AiChatRequestDto requestDto, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(aiService.chat(authentication.getName(), requestDto)));
    }

    @DeleteMapping("/chat")
    @Operation(summary = "AI 대화 초기화", description = "현재 학습 언어의 AI 대화 기록을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> clearChat(Authentication authentication) {
        aiService.clearChat(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
