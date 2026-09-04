package com.qurve.wrongnote.controller;

import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.qurve.wrongnote.dto.request.WrongNoteReviewCompleteRequestDto;
import com.qurve.wrongnote.dto.response.WrongNoteReviewCompleteResponseDto;
import com.qurve.wrongnote.service.WrongNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wrong-notes")
@Tag(name = "오답노트", description = "오답노트 복습 완료 처리 API")
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;

    /**
     * 오답노트 학습 종료 처리
     */
    @PostMapping("/reviews/complete")
    @Operation(summary = "오답노트 복습 완료", description = "선택한 오답 문제의 복습을 완료 처리하고 관련 보상을 반영합니다.")
    public ResponseEntity<ApiResponse<WrongNoteReviewCompleteResponseDto>> completeReview(
            @Valid @RequestBody WrongNoteReviewCompleteRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                wrongNoteService.completeReview(authentication.getName(), requestDto)
        ));
    }
}
