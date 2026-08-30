package com.qurve.wrongnote.controller;

import com.qurve.global.common.ApiResponse;
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
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;

    /**
     * 오답노트 학습 종료 처리
     */
    @PostMapping("/reviews/complete")
    public ResponseEntity<ApiResponse<WrongNoteReviewCompleteResponseDto>> completeReview(
            @Valid @RequestBody WrongNoteReviewCompleteRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                wrongNoteService.completeReview(authentication.getName(), requestDto)
        ));
    }
}
