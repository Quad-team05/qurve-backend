package com.qurve.problem.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.global.util.CompletionKeyGenerator;
import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemSetCompletion;
import com.qurve.problem.domain.ProblemSubmission;
import com.qurve.problem.dto.request.ProblemSetCompleteRequestDto;
import com.qurve.problem.dto.response.ProblemSetCompleteResponseDto;
import com.qurve.problem.repository.ProblemRepository;
import com.qurve.problem.repository.ProblemSetCompletionRepository;
import com.qurve.problem.repository.ProblemSubmissionRepository;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 문제 세트 완료와 만점 여부를 사용자 제출 이력으로 검증합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemSetService {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final ProblemSetCompletionRepository problemSetCompletionRepository;

    /**
     * 문제 세트의 모든 문제 제출 여부를 검증한 뒤 완료 기록을 저장합니다.
     *
     * * 동일한 문제 목록은 한 번만 완료 처리되어, 이후 XP 적립을 연결해도 중복 적립되지 않습니다.
     *
     * @param loginId 로그인 ID
     * @param requestDto 완료할 문제 세트의 문제 ID 목록
     * @return 완료된 문제 세트 정보
     * @throws BusinessException 유저, 문제 또는 제출 이력이 없는 경우
     */
    @Transactional
    public ProblemSetCompleteResponseDto complete(String loginId, ProblemSetCompleteRequestDto requestDto) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Long> problemIds = requestDto.getProblemIds().stream()
                .distinct()
                .sorted()
                .toList();
        String setKey = CompletionKeyGenerator.generate(problemIds);

        ProblemSetCompletion existingCompletion = problemSetCompletionRepository.findByUserAndSetKey(user, setKey)
                .orElse(null);
        if (existingCompletion != null) {
            return ProblemSetCompleteResponseDto.from(existingCompletion);
        }

        List<Problem> problems = problemRepository.findAllById(problemIds);
        if (problems.size() != problemIds.size()) {
            throw new BusinessException(ErrorCode.PROBLEM_NOT_FOUND);
        }

        Map<Long, Problem> problemById = problems.stream()
                .collect(Collectors.toMap(Problem::getProblemId, Function.identity()));

        List<ProblemSubmission> latestSubmissions = problemIds.stream()
                .map(problemId -> problemSubmissionRepository
                        .findFirstByUserAndProblemOrderBySubmissionIdDesc(user, problemById.get(problemId))
                        .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_SET_SUBMISSION_INCOMPLETE)))
                .toList();

        int correctCount = (int) latestSubmissions.stream()
                .filter(ProblemSubmission::isCorrect)
                .count();

        ProblemSetCompletion completion = problemSetCompletionRepository.save(ProblemSetCompletion.builder()
                .user(user)
                .setKey(setKey)
                .problemCount(problemIds.size())
                .correctCount(correctCount)
                .perfect(correctCount == problemIds.size())
                .build());

        return ProblemSetCompleteResponseDto.from(completion);
    }
}
