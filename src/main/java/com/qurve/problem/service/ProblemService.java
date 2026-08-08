package com.qurve.problem.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemChoice;
import com.qurve.problem.dto.request.ProblemListRequestDto;
import com.qurve.problem.dto.response.ProblemChoiceResponseDto;
import com.qurve.problem.dto.response.ProblemListResponseDto;
import com.qurve.problem.dto.response.ProblemResponseDto;
import com.qurve.problem.repository.ProblemChoiceRepository;
import com.qurve.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemChoiceRepository problemChoiceRepository;

    /**
     * 문제 목록 조회
     *
     * * JLPT 레벨, 문제 유형, 세부 유형을 기준으로 문제와 선택지를 함께 조회한다.
     *
     * * 문제 풀이 화면에서는 정답과 해설을 숨기고 문제 본문과 선택지만 반환한다.
     *
     * @param requestDto 문제 조회 조건
     * @return 조회 조건에 맞는 문제 목록
     * @throws BusinessException 조회 조건에 맞는 문제가 없는 경우
     */
    public ProblemListResponseDto findAll(ProblemListRequestDto requestDto) {
        String normalizedLevel = normalizeKeyword(requestDto.getLevel());
        String normalizedCategory = normalizeKeyword(requestDto.getCategory());
        String normalizedSubType = normalizeKeyword(requestDto.getSubType());

        List<Problem> problems = problemRepository.findAllByLevelAndCategoryAndSubTypeOrderByProblemIdAsc(
                normalizedLevel,
                normalizedCategory,
                normalizedSubType
        );

        if (problems.isEmpty()) {
            throw new BusinessException(ErrorCode.PROBLEM_NOT_FOUND);
        }

        if (requestDto.getCount() != null) {
            problems = problems.stream()
                    .limit(requestDto.getCount())
                    .toList();
        }

        List<ProblemChoice> problemChoices = problemChoiceRepository
                .findAllByProblemsOrderByProblemIdAscChoiceNumberAsc(problems);

        Map<Long, List<ProblemChoiceResponseDto>> choiceMap = problemChoices.stream()
                .collect(Collectors.groupingBy(
                        problemChoice -> problemChoice.getProblem().getProblemId(),
                        Collectors.mapping(ProblemChoiceResponseDto::from, Collectors.toList())
                ));

        List<ProblemResponseDto> problemResponseDtos = problems.stream()
                .map(problem -> ProblemResponseDto.from(
                        problem,
                        choiceMap.getOrDefault(problem.getProblemId(), List.of())
                ))
                .toList();

        return ProblemListResponseDto.of(
                normalizedLevel,
                normalizedCategory,
                normalizedSubType,
                problemResponseDtos
        );
    }

    /**
     * 조회 키워드 정규화
     *
     * * 저장된 문제 데이터와 동일한 형식으로 비교하기 위해
     * 공백을 제거하고 대문자로 변환한다.
     *
     * @param value 요청으로 전달된 조회 값
     * @return 정규화된 조회 값
     */
    private String normalizeKeyword(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
