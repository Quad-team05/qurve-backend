package com.qurve.problem.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.problem.dto.request.ProblemListRequestDto;
import com.qurve.problem.dto.response.ProblemListResponseDto;
import com.qurve.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
}
