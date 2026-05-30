package com.qurve.level.service;

import com.qurve.level.dto.response.OptionDto;
import com.qurve.level.dto.response.PreQuestionResponseDto;
import com.qurve.level.dto.response.QuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LevelService {

    /**
     * 사전 레벨 테스트 질문 조회
     *
     * * 레벨 테스트 이전에 사용자의 학습 경험과
     * 기초 문자 이해도를 파악하기 위한 사전 질문 목록을 반환한다.
     *
     * @return 사전 레벨 테스트 질문 목록
     */
    public PreQuestionResponseDto getPreQuestions() {

        List<OptionDto> option1 = List.of(
                new OptionDto(1, "처음 시작해요 (0개월)"),
                new OptionDto(2, "3개월 미만"),
                new OptionDto(3, "3개월 ~ 1년 미만"),
                new OptionDto(4, "1년 이상")
        );

        List<OptionDto> option2 = List.of(
                new OptionDto(1, "둘 다 읽을 수 있어요"),
                new OptionDto(2, "히라가나만 읽을 수 있어요"),
                new OptionDto(3, "아직 어려워요")
        );

        List<QuestionDto> questions = List.of(
                new QuestionDto(1, "일본어를 배워본 기간이 얼마나 되나요?", option1),
                new QuestionDto(2, "히라가나·가타카나를 읽을 수 있나요?", option2)
        );

        return new PreQuestionResponseDto(questions);
    }
}
