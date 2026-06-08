package com.qurve.vocabulary.controller;

import com.qurve.global.common.ApiResponse;
import com.qurve.vocabulary.dto.response.UnitProgressResponseDto;
import com.qurve.vocabulary.service.VocabularyService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
