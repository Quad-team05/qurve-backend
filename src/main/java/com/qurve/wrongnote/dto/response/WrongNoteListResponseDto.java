package com.qurve.wrongnote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WrongNoteListResponseDto {

    private YearMonth yearMonth;
    private List<LocalDate> wrongNoteDates;
    private List<WrongNoteSummaryResponseDto> wrongNotes;

    public static WrongNoteListResponseDto of(
            YearMonth yearMonth,
            List<LocalDate> wrongNoteDates,
            List<WrongNoteSummaryResponseDto> wrongNotes
    ) {
        return WrongNoteListResponseDto.builder()
                .yearMonth(yearMonth)
                .wrongNoteDates(wrongNoteDates)
                .wrongNotes(wrongNotes)
                .build();
    }
}
