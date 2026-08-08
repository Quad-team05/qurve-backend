package com.qurve.problem.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.problem.dto.request.ProblemListRequestDto;
import com.qurve.problem.dto.request.ProblemSubmitRequestDto;
import com.qurve.problem.dto.response.ProblemListResponseDto;
import com.qurve.problem.dto.response.ProblemSolutionListResponseDto;
import com.qurve.problem.dto.response.ProblemSubmitResponseDto;
import com.qurve.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
