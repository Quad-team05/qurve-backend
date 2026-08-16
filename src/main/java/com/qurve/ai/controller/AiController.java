package com.qurve.ai.controller;

import com.qurve.ai.dto.request.AiChatRequestDto;
import com.qurve.ai.dto.response.AiChatResponseDto;
import com.qurve.ai.service.AiService;
import com.qurve.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponseDto>> chat(@Valid @RequestBody AiChatRequestDto requestDto, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(aiService.chat(authentication.getName(), requestDto)));
    }

    @DeleteMapping("/chat")
    public ResponseEntity<ApiResponse<Void>> clearChat(Authentication authentication) {
        aiService.clearChat(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}