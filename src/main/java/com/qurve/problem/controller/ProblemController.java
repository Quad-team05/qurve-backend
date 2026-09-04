package com.qurve.problem.controller;

import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.qurve.problem.dto.request.ProblemListRequestDto;
import com.qurve.problem.dto.request.ProblemSetCompleteRequestDto;
import com.qurve.problem.dto.request.ProblemSubmitRequestDto;
import com.qurve.problem.dto.response.ProblemAccuracyResponseDto;
import com.qurve.problem.dto.response.ProblemAccuracyTrendResponseDto;
import com.qurve.problem.dto.response.ProblemListResponseDto;
import com.qurve.problem.dto.response.ProblemResponseDto;
import com.qurve.problem.dto.response.ProblemSolutionListResponseDto;
import com.qurve.problem.dto.response.ProblemSubmitResponseDto;
import com.qurve.problem.dto.response.ProblemSetCompleteResponseDto;
import com.qurve.problem.service.ProblemSetService;
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
@Tag(name = "문제", description = "JLPT 문제 조회, 제출, 풀이 이력, 북마크 및 정답률 API")
public class ProblemController {

    private final ProblemService problemService;
    private final ProblemSetService problemSetService;

    @GetMapping
    @Operation(summary = "문제 목록 조회", description = "레벨, 카테고리, 세부 유형 조건으로 문제와 선택지를 조회합니다.")
    public ResponseEntity<ApiResponse<ProblemListResponseDto>> findAll(
            @Valid @ModelAttribute ProblemListRequestDto requestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(problemService.findAll(requestDto)));
    }

    @PostMapping("/{problemId}/submit")
    @Operation(summary = "문제 답안 제출", description = "선택한 답안을 채점하고 정답, 해설, 결과를 반환합니다.")
    public ResponseEntity<ApiResponse<ProblemSubmitResponseDto>> submit(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemSubmitRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.submit(authentication.getName(), problemId, requestDto))
        );
    }

    /**
     * 문제 세트 학습 종료 처리
     */
    @PostMapping("/sets/complete")
    @Operation(summary = "문제 세트 완료", description = "문제 세트의 모든 제출을 확인하고 세트 완료 및 관련 XP를 처리합니다.")
    public ResponseEntity<ApiResponse<ProblemSetCompleteResponseDto>> completeSet(
            @Valid @RequestBody ProblemSetCompleteRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                problemSetService.complete(authentication.getName(), requestDto)
        ));
    }

    @GetMapping("/{problemId}/solution")
    @Operation(summary = "문제 풀이 이력 조회", description = "제출한 문제의 정답과 해설, 사용자 제출 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<ProblemSolutionListResponseDto>> findSolution(
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.findSolution(authentication.getName(), problemId))
        );
    }

    @GetMapping("/accuracy")
    @Operation(summary = "전체 정답률 조회", description = "로그인한 사용자의 전체 문제 제출 정답률을 조회합니다.")
    public ResponseEntity<ApiResponse<ProblemAccuracyResponseDto>> findAccuracy(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.findAccuracy(authentication.getName()))
        );
    }

    @GetMapping("/accuracy/trend")
    @Operation(summary = "최근 7일 정답률 조회", description = "KST 기준 최근 7일의 일별 문제 풀이 정답률을 조회합니다.")
    public ResponseEntity<ApiResponse<ProblemAccuracyTrendResponseDto>> findAccuracyTrend(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.findAccuracyTrend(authentication.getName()))
        );
    }

    @PostMapping("/bookmarks/{problemId}")
    @Operation(summary = "문제 북마크 추가", description = "문제를 사용자 북마크 목록에 추가합니다.")
    public ResponseEntity<ApiResponse<Void>> addBookmark(
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        problemService.addBookmark(authentication.getName(), problemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/bookmarks/{problemId}")
    @Operation(summary = "문제 북마크 삭제", description = "문제를 사용자 북마크 목록에서 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            @PathVariable Long problemId,
            Authentication authentication
    ) {
        problemService.removeBookmark(authentication.getName(), problemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/bookmarks")
    @Operation(summary = "문제 북마크 목록 조회", description = "로그인한 사용자가 북마크한 문제 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProblemResponseDto>>> findBookmarks(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.findBookmarks(authentication.getName()))
        );
    }
}
