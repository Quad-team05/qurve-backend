package com.qurve.problem.service;

import com.qurve.badge.service.BadgeService;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemBookmark;
import com.qurve.problem.domain.ProblemChoice;
import com.qurve.problem.domain.ProblemSubmission;
import com.qurve.problem.dto.request.ProblemListRequestDto;
import com.qurve.problem.dto.request.ProblemSubmitRequestDto;
import com.qurve.problem.dto.response.ProblemChoiceResponseDto;
import com.qurve.problem.dto.response.ProblemAccuracyResponseDto;
import com.qurve.problem.dto.response.ProblemListResponseDto;
import com.qurve.problem.dto.response.ProblemResponseDto;
import com.qurve.problem.dto.response.ProblemSolutionListResponseDto;
import com.qurve.problem.dto.response.ProblemSolutionResponseDto;
import com.qurve.problem.dto.response.ProblemSubmitResponseDto;
import com.qurve.problem.repository.ProblemBookmarkRepository;
import com.qurve.problem.repository.ProblemChoiceRepository;
import com.qurve.problem.repository.ProblemRepository;
import com.qurve.problem.repository.ProblemSubmissionRepository;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
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
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final ProblemBookmarkRepository problemBookmarkRepository;
    private final UserRepository userRepository;
    private final BadgeService badgeService;

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
     * 문제 답안 제출
     *
     * * 사용자가 선택한 선택지 번호를 정답 번호와 비교하고
     * 정답 선택지와 해설을 함께 반환한다.
     *
     * @param loginId 로그인 ID
     * @param problemId 제출 대상 문제 ID
     * @param requestDto 제출한 선택지 번호
     * @return 채점 결과와 정답 정보
     * @throws BusinessException 유저, 문제가 없거나 선택지 번호가 유효하지 않은 경우
     */
    @Transactional
    public ProblemSubmitResponseDto submit(String loginId, Long problemId, ProblemSubmitRequestDto requestDto) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        List<ProblemChoice> problemChoices = problemChoiceRepository.findAllByProblemOrderByChoiceNumberAsc(problem);

        boolean isValidSelectedChoice = problemChoices.stream()
                .anyMatch(problemChoice -> problemChoice.getChoiceNumber().equals(requestDto.getSelectedChoiceNumber()));

        if (!isValidSelectedChoice) {
            throw new BusinessException(ErrorCode.INVALID_PROBLEM_CHOICE);
        }

        ProblemChoice answerChoice = problemChoices.stream()
                .filter(problemChoice -> problemChoice.getChoiceNumber().equals(problem.getAnswerIndex()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_ANSWER_CHOICE_NOT_FOUND));

        ProblemSubmission problemSubmission = problemSubmissionRepository.save(ProblemSubmission.builder()
                .user(user)
                .problem(problem)
                .selectedChoiceNumber(requestDto.getSelectedChoiceNumber())
                .answerChoiceNumber(problem.getAnswerIndex())
                .correct(problem.getAnswerIndex().equals(requestDto.getSelectedChoiceNumber()))
                .build());
        badgeService.evaluate(user);

        return ProblemSubmitResponseDto.of(problemSubmission, answerChoice);
    }

    /**
     * 문제 정답 풀이 이력 조회
     *
     * * 로그인한 사용자의 제출 이력을 최신순으로 조회하고
     * 각 제출 이력별 정답과 해설을 반환한다.
     *
     * @param loginId 로그인 ID
     * @param problemId 조회 대상 문제 ID
     * @return 제출 이력별 정답 풀이 목록
     * @throws BusinessException 유저, 문제, 제출 이력이 없거나 정답 선택지가 유효하지 않은 경우
     */
    public ProblemSolutionListResponseDto findSolution(String loginId, Long problemId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        List<ProblemSubmission> problemSubmissions = problemSubmissionRepository
                .findAllByUserAndProblemOrderBySubmissionIdDesc(user, problem);

        if (problemSubmissions.isEmpty()) {
            throw new BusinessException(ErrorCode.PROBLEM_SUBMISSION_NOT_FOUND);
        }

        ProblemChoice answerChoice = problemChoiceRepository
                .findByProblemAndChoiceNumber(problem, problem.getAnswerIndex())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PROBLEM_CHOICE));

        List<ProblemSolutionResponseDto> solutions = problemSubmissions.stream()
                .map(problemSubmission -> ProblemSolutionResponseDto.of(problemSubmission, answerChoice))
                .toList();

        return ProblemSolutionListResponseDto.of(problem.getProblemId(), solutions);
    }

    /**
     * 문제풀이 정답률 조회
     *
     * * 로그인한 사용자의 전체 문제 제출 수와 정답 수를 기준으로
     * 정답률을 퍼센트 단위로 계산해 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 문제풀이 정답률 통계
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public ProblemAccuracyResponseDto findAccuracy(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        int totalSubmissionCount = (int) problemSubmissionRepository.countByUser(user);
        int correctSubmissionCount = (int) problemSubmissionRepository.countByUserAndCorrectTrue(user);

        return ProblemAccuracyResponseDto.of(totalSubmissionCount, correctSubmissionCount);
    }

    /**
     * 문제 북마크 추가
     *
     * * 문제 풀이 중 북마크 버튼 클릭 시 해당 문제를 북마크에 추가한다.
     * * 이미 북마크된 문제인 경우 예외를 발생시킨다.
     *
     * @param loginId 로그인 ID
     * @param problemId 북마크할 문제 ID
     * @throws BusinessException 유저나 문제가 없거나 이미 북마크된 문제인 경우
     */
    @Transactional
    public void addBookmark(String loginId, Long problemId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        if (problemBookmarkRepository.existsByUserAndProblem(user, problem)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PROBLEM_BOOKMARK);
        }

        problemBookmarkRepository.save(ProblemBookmark.builder()
                .user(user)
                .problem(problem)
                .build());
    }

    /**
     * 문제 북마크 삭제
     *
     * * 북마크된 문제를 북마크에서 제거한다.
     *
     * @param loginId 로그인 ID
     * @param problemId 북마크 삭제할 문제 ID
     * @throws BusinessException 유저, 문제, 북마크가 존재하지 않는 경우
     */
    @Transactional
    public void removeBookmark(String loginId, Long problemId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        ProblemBookmark problemBookmark = problemBookmarkRepository.findByUserAndProblem(user, problem)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_BOOKMARK_NOT_FOUND));

        problemBookmarkRepository.delete(problemBookmark);
    }

    /**
     * 문제 북마크 목록 조회
     *
     * * 로그인한 사용자가 북마크한 문제 목록을 최신순으로 조회한다.
     * * 문제 풀이 화면과 동일하게 정답과 해설은 숨기고 문제 본문과 선택지만 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 북마크한 문제 목록
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public List<ProblemResponseDto> findBookmarks(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Problem> problems = problemBookmarkRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(ProblemBookmark::getProblem)
                .toList();

        if (problems.isEmpty()) {
            return List.of();
        }

        List<ProblemChoice> problemChoices = problemChoiceRepository
                .findAllByProblemsOrderByProblemIdAscChoiceNumberAsc(problems);

        Map<Long, List<ProblemChoiceResponseDto>> choiceMap = problemChoices.stream()
                .collect(Collectors.groupingBy(
                        problemChoice -> problemChoice.getProblem().getProblemId(),
                        Collectors.mapping(ProblemChoiceResponseDto::from, Collectors.toList())
                ));

        return problems.stream()
                .map(problem -> ProblemResponseDto.from(
                        problem,
                        choiceMap.getOrDefault(problem.getProblemId(), List.of())
                ))
                .toList();
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
