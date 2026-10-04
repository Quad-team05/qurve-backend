package com.qurve.problem.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProblemListRequestDto {

    /** 기존 JLPT 문제 조회용 레벨(N1~N5) */
    private String level;

    /** 문제 데이터 언어(JA, EN) */
    private String language;

    /** 영어 문제의 CEFR 레벨(A1~C2) */
    private String cefrLevel;

    /** 영어 문제의 QURVE 레벨(Lv1~Lv10). level과 함께 전달하지 않는다. */
    private String qurveLevel;

    private String usageType;

    private String category;

    private String subType;

    private String topic;

    @Positive(message = "count는 1 이상의 값이어야 합니다.")
    @Max(value = 50, message = "count는 50 이하여야 합니다.")
    private Integer count;

    @PositiveOrZero(message = "offset은 0 이상의 값이어야 합니다.")
    private Integer offset;
}
