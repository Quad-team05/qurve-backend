package com.qurve.vocabulary.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.vocabulary.dto.response.UnitProgressResponseDto;
import com.qurve.vocabulary.dto.response.UnitWordStudyResponseDto;
import com.qurve.vocabulary.service.VocabularyService;
import jakarta.validation.constraints.NotBlank;
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
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping("/units")
    public ResponseEntity<ApiResponse<List<UnitProgressResponseDto>>> getUnitList(@NotBlank @RequestParam("level") String level, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getUnitList(authentication.getName(), level)));
    }

    @GetMapping("/units/{unitNumber}/words")
    public ApiResponse<UnitWordStudyResponseDto> getUnitWords(@PathVariable Integer unitNumber, @RequestParam String level, Authentication authentication) {
        String loginId = authentication.getName();

        UnitWordStudyResponseDto response = vocabularyService.getUnitWords(loginId, level, unitNumber);

        return ApiResponse.success(response);
    }

    @PostMapping("/bookmarks/{wordId}")
    public ResponseEntity<ApiResponse<Void>> addBookmark(@PathVariable Long wordId, Authentication authentication) {
        vocabularyService.addBookmark(authentication.getName(), wordId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/bookmarks/{wordId}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(@PathVariable Long wordId, Authentication authentication) {
        vocabularyService.removeBookmark(authentication.getName(), wordId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/units/{unitNumber}/start")
    public ResponseEntity<ApiResponse<Void>> startUnit(@PathVariable Integer unitNumber, @RequestParam String level, Authentication authentication) {
        vocabularyService.startUnit(authentication.getName(), level, unitNumber);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/units/{unitNumber}/complete")
    public ResponseEntity<ApiResponse<Void>> completeUnit(@PathVariable Integer unitNumber, @RequestParam String level, Authentication authentication) {
        vocabularyService.completeUnit(authentication.getName(), level, unitNumber);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
