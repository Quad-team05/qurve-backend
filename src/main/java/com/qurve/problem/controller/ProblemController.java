package com.qurve.problem.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.problem.dto.request.ProblemListRequestDto;
import com.qurve.problem.dto.request.ProblemSubmitRequestDto;
import com.qurve.problem.dto.response.ProblemAccuracyResponseDto;
import com.qurve.problem.dto.response.ProblemAccuracyTrendResponseDto;
import com.qurve.problem.dto.response.ProblemListResponseDto;
import com.qurve.problem.dto.response.ProblemResponseDto;
import com.qurve.problem.dto.response.ProblemSolutionListResponseDto;
import com.qurve.problem.dto.response.ProblemSubmitResponseDto;
import com.qurve.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProblemListResponseDto>> findAll(
            @Valid @ModelAttribute ProblemListRequestDto requestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(problemService.findAll(requestDto)));
    }

    @PostMapping("/{problemId}/submit")
    public ResponseEntity<ApiResponse<ProblemSubmitResponseDto>> submit(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemSubmitRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.submit(authentication.getName(), problemId, requestDto))
        );
    }

    @GetMapping("/{problemId}/solution")
    public ResponseEntity<ApiResponse<ProblemSolutionListResponseDto>> findSolution(
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.findSolution(authentication.getName(), problemId))
        );
    }

    @GetMapping("/accuracy")
    public ResponseEntity<ApiResponse<ProblemAccuracyResponseDto>> findAccuracy(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.findAccuracy(authentication.getName()))
        );
    }

    @GetMapping("/accuracy/trend")
    public ResponseEntity<ApiResponse<ProblemAccuracyTrendResponseDto>> findAccuracyTrend(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.findAccuracyTrend(authentication.getName()))
        );
    }

    @PostMapping("/bookmarks/{problemId}")
    public ResponseEntity<ApiResponse<Void>> addBookmark(
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        problemService.addBookmark(authentication.getName(), problemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/bookmarks/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        problemService.removeBookmark(authentication.getName(), problemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<List<ProblemResponseDto>>> findBookmarks(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.findBookmarks(authentication.getName()))
        );
    }
}
