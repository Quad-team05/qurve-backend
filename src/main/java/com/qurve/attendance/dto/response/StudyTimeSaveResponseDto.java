package com.qurve.attendance.dto.response;

import com.qurve.attendance.domain.StudyStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StudyTimeSaveResponseDto {
    private int addedStudyTimeMinutes;
    private int totalStudyTimeMinutes;

    public static StudyTimeSaveResponseDto of(int addedStudyTimeMinutes, StudyStatistics studyStatistics) {
        return StudyTimeSaveResponseDto.builder()
                .addedStudyTimeMinutes(addedStudyTimeMinutes)
                .totalStudyTimeMinutes(studyStatistics.getTotalStudyTime())
                .build();
    }
}
