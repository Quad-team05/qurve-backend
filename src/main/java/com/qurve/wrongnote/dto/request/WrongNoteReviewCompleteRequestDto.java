package com.qurve.wrongnote.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class WrongNoteReviewCompleteRequestDto {

    @NotEmpty(message = "problemIds는 필수입니다.")
    @Size(max = 100, message = "오답노트는 최대 100문제까지 완료 처리할 수 있습니다.")
    private List<@NotNull Long> problemIds;
}
