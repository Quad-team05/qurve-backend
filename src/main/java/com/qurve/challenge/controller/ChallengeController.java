package com.qurve.challenge.controller;

import com.qurve.challenge.dto.request.ChallengeCreateRequestDto;
import com.qurve.challenge.dto.request.ChallengeUpdateRequestDto;
import com.qurve.challenge.dto.response.ChallengeCreateResponseDto;
import com.qurve.challenge.dto.response.ChallengeMainResponseDto;
import com.qurve.challenge.dto.response.ChallengeManagementResponseDto;
import com.qurve.challenge.dto.response.ChallengeUpdateResponseDto;
import com.qurve.challenge.service.ChallengeService;
import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "챌린지", description = "개인 학습 챌린지 생성, 관리, 진행도 조회 API")
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/main")
    @Operation(summary = "메인 챌린지 조회", description = "사용자의 현재 학습 언어에 해당하는 진행 중 챌린지와 달성률을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ChallengeMainResponseDto>>> findAllForMain(
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(challengeService.findAllForMain(authentication.getName())));
    }

    @GetMapping
    @Operation(
            summary = "챌린지 관리 현황 조회",
            description = "사용자의 연속 학습일과 현재 학습 언어의 챌린지 현황을 조회합니다. "
                    + "전체 달성률은 현재 학습 언어의 진행 중 챌린지를 기준으로 계산하며, "
                    + "진행 중 및 완료 챌린지 목록을 반환합니다."
    )
    public ResponseEntity<ApiResponse<ChallengeManagementResponseDto>> findManagement(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        challengeService.findManagement(authentication.getName())
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "챌린지 생성",
            description = "목표 유형, 목표값, 기간을 입력해 사용자의 현재 학습 언어로 챌린지를 생성합니다. "
                    + "생성 시 저장된 챌린지의 학습 언어는 이후 사용자 설정을 변경해도 유지됩니다. "
                    + "단어 챌린지는 goalType에 WORD_COUNT를, targetValue에 학습할 단어 개수를 입력하세요."
    )
    public ResponseEntity<ApiResponse<ChallengeCreateResponseDto>> createChallenge(
            @Valid @RequestBody ChallengeCreateRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        challengeService.createChallenge(requestDto, authentication.getName())
                )
        );
    }

    @PatchMapping("/{challengeId}")
    @Operation(
            summary = "챌린지 수정",
            description = "사용자의 현재 학습 언어에 속한 본인의 진행 중 챌린지에서 제목, 목표값, 기간을 수정합니다. "
                    + "목표 유형과 학습 언어는 변경할 수 없습니다."
    )
    public ResponseEntity<ApiResponse<ChallengeUpdateResponseDto>> updateChallenge(
            @PathVariable Long challengeId,
            @Valid @RequestBody ChallengeUpdateRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        challengeService.update(challengeId, requestDto, authentication.getName())
                )
        );
    }

    @DeleteMapping("/{challengeId}")
    @Operation(
            summary = "챌린지 삭제",
            description = "사용자의 현재 학습 언어에 속한 본인의 챌린지와 연결된 진행도 정보를 함께 삭제합니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteChallenge(
            @PathVariable Long challengeId,
            Authentication authentication
    ) {
        challengeService.delete(challengeId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
