package com.qurve.vocabulary.controller;

import com.qurve.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.qurve.vocabulary.dto.request.ChallengeWordCompleteRequestDto;
import com.qurve.vocabulary.dto.response.ChallengeWordCompleteResponseDto;
import com.qurve.vocabulary.dto.response.UnitProgressResponseDto;
import com.qurve.vocabulary.dto.response.UnitWordResponseDto;
import com.qurve.vocabulary.dto.response.UnitWordStudyResponseDto;
import com.qurve.vocabulary.service.VocabularyService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/vocabularies")
@Tag(name = "단어", description = "단어 유닛 학습, 북마크, 챌린지 단어 학습 API")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping("/units")
    @Operation(summary = "단어 유닛 목록 조회", description = "JLPT 레벨별 단어 유닛과 사용자 학습 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<List<UnitProgressResponseDto>>> getUnitList(@NotBlank @RequestParam("level") String level, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getUnitList(authentication.getName(), level)));
    }

    @GetMapping("/units/{unitNumber}/words")
    @Operation(summary = "유닛 단어 조회", description = "선택한 레벨과 유닛의 학습 단어 목록을 조회합니다.")
    public ApiResponse<UnitWordStudyResponseDto> getUnitWords(@PathVariable Integer unitNumber, @RequestParam String level, Authentication authentication) {
        String loginId = authentication.getName();

        UnitWordStudyResponseDto response = vocabularyService.getUnitWords(loginId, level, unitNumber);

        return ApiResponse.success(response);
    }

    @PostMapping("/bookmarks/{wordId}")
    @Operation(summary = "단어 북마크 추가", description = "단어를 사용자 북마크 목록에 추가합니다.")
    public ResponseEntity<ApiResponse<Void>> addBookmark(@PathVariable Long wordId, Authentication authentication) {
        vocabularyService.addBookmark(authentication.getName(), wordId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/bookmarks/{wordId}")
    @Operation(summary = "단어 북마크 삭제", description = "단어를 사용자 북마크 목록에서 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(@PathVariable Long wordId, Authentication authentication) {
        vocabularyService.removeBookmark(authentication.getName(), wordId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/units/{unitNumber}/start")
    @Operation(summary = "유닛 학습 시작", description = "단어 유닛 상태를 학습 중으로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> startUnit(@PathVariable Integer unitNumber, @RequestParam String level, Authentication authentication) {
        vocabularyService.startUnit(authentication.getName(), level, unitNumber);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/units/{unitNumber}/complete")
    @Operation(summary = "유닛 학습 완료", description = "단어 유닛을 완료 처리하고 새 학습 단어·XP·챌린지 진행도를 반영합니다.")
    public ResponseEntity<ApiResponse<Void>> completeUnit(@PathVariable Integer unitNumber, @RequestParam String level, Authentication authentication) {
        vocabularyService.completeUnit(authentication.getName(), level, unitNumber);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/challenge-words")
    @Operation(summary = "챌린지 단어 조회", description = "진행 중인 단어 암기 챌린지의 목표 개수만큼 무작위 단어를 조회합니다.")
    public ResponseEntity<ApiResponse<List<UnitWordResponseDto>>> getChallengeWords(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getChallengeWords(authentication.getName())));
    }

    @PostMapping("/challenge-words/complete")
    @Operation(summary = "챌린지 단어 완료", description = "챌린지에서 학습한 단어를 완료 처리하고 새 단어 수만큼 진행도를 반영합니다.")
    public ResponseEntity<ApiResponse<ChallengeWordCompleteResponseDto>> completeChallengeWords(
            @Valid @RequestBody ChallengeWordCompleteRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(vocabularyService.completeChallengeWords(authentication.getName(), requestDto))
        );
    }

    @GetMapping("/bookmarks")
    @Operation(summary = "단어 북마크 목록 조회", description = "로그인한 사용자가 북마크한 단어 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UnitWordResponseDto>>> getBookmarks(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getBookmarks(authentication.getName())));
    }
}
