package com.qurve.wrongnote.controller;

import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.qurve.wrongnote.dto.request.WrongNoteReviewCompleteRequestDto;
import com.qurve.wrongnote.dto.response.WrongNoteReviewCompleteResponseDto;
import com.qurve.wrongnote.dto.response.WrongNoteListResponseDto;
import com.qurve.wrongnote.dto.response.WrongNoteSolutionResponseDto;
import com.qurve.wrongnote.service.WrongNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wrong-notes")
@Tag(name = "오답노트", description = "오답노트 목록, 문제 풀이, 복습 완료 처리 API")
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;

    @GetMapping
    @Operation(summary = "오답노트 목록 조회", description = "현재 학습 언어의 선택한 월 오답 날짜와 문제별 오답노트 목록을 조회합니다. yearMonth를 생략하면 KST 기준 이번 달을 조회합니다.")
    public ResponseEntity<ApiResponse<WrongNoteListResponseDto>> findAll(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth yearMonth,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                wrongNoteService.findAll(authentication.getName(), yearMonth)
        ));
    }

    @GetMapping("/{problemId}/solution")
    @Operation(summary = "오답노트 문제 풀이 조회", description = "현재 학습 언어의 본인 오답 문제에 대해 마지막 오답 선택지, 정답, 해설과 전체 선택지를 조회합니다.")
    public ResponseEntity<ApiResponse<WrongNoteSolutionResponseDto>> findSolution(
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                wrongNoteService.findSolution(authentication.getName(), problemId)
        ));
    }

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
