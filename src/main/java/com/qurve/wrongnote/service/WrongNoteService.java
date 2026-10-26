package com.qurve.wrongnote.service;

import com.qurve.attendance.service.AttendanceService;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.enums.XpActionType;
import com.qurve.global.exception.BusinessException;
import com.qurve.global.util.CompletionKeyGenerator;
import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemChoice;
import com.qurve.problem.domain.ProblemSubmission;
import com.qurve.problem.repository.ProblemRepository;
import com.qurve.problem.repository.ProblemChoiceRepository;
import com.qurve.problem.repository.ProblemSubmissionRepository;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.xp.service.XpService;
import com.qurve.wrongnote.domain.WrongNote;
import com.qurve.wrongnote.domain.WrongNoteReview;
import com.qurve.wrongnote.dto.request.WrongNoteReviewCompleteRequestDto;
import com.qurve.wrongnote.dto.response.WrongNoteReviewCompleteResponseDto;
import com.qurve.wrongnote.dto.response.WrongNoteListResponseDto;
import com.qurve.wrongnote.dto.response.WrongNoteSolutionResponseDto;
import com.qurve.wrongnote.dto.response.WrongNoteSummaryResponseDto;
import com.qurve.wrongnote.repository.WrongNoteRepository;
import com.qurve.wrongnote.repository.WrongNoteReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 문제 오답 기록과 오답노트 복습 완료 상태를 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WrongNoteService {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final ProblemChoiceRepository problemChoiceRepository;
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final WrongNoteRepository wrongNoteRepository;
    private final WrongNoteReviewRepository wrongNoteReviewRepository;
    private final XpService xpService;
    private final AttendanceService attendanceService;

    /**
     * 최초 오답 문제를 오답노트에 저장합니다.
     */
    @Transactional
    public void saveWrongAnswer(User user, Problem problem) {
        if (wrongNoteRepository.findByUserAndProblem(user, problem).isPresent()) {
            return;
        }

        wrongNoteRepository.save(WrongNote.builder()
                .user(user)
                .problem(problem)
                .build());
    }

    /**
     * 오답노트 복습을 마친 뒤 정답을 맞힌 문제를 표시합니다.
     */
    @Transactional
    public void markRetryCorrect(User user, Problem problem) {
        wrongNoteRepository.findByUserAndProblem(user, problem)
                .ifPresent(wrongNote -> wrongNote.markRetryCorrect(LocalDateTime.now()));
    }

    /**
     * 월별 오답노트 목록 조회
     *
     * * 현재 학습 언어에 해당하는 오답 문제만 조회한다.
     * * 선택한 월에 실제로 틀린 제출의 날짜와 문제별 카드 정보를 반환한다.
     *
     * @param loginId 로그인 ID
     * @param yearMonth 조회할 연월
     * @return 월별 오답노트 목록
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public WrongNoteListResponseDto findAll(String loginId, YearMonth yearMonth) {
        User user = findUserByLoginId(loginId);
        YearMonth targetYearMonth = yearMonth == null ? YearMonth.now(KST_ZONE) : yearMonth;
        LocalDateTime startDateTime = targetYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = targetYearMonth.plusMonths(1).atDay(1).atStartOfDay();
        LearningLanguage learningLanguage = resolveLearningLanguage(user);

        List<ProblemSubmission> wrongSubmissions = problemSubmissionRepository
                .findAllWrongByUserAndCreatedAtBetween(user, startDateTime, endDateTime)
                .stream()
                .filter(submission -> submission.getProblem().belongsTo(learningLanguage))
                .toList();

        if (wrongSubmissions.isEmpty()) {
            return WrongNoteListResponseDto.of(targetYearMonth, List.of(), List.of());
        }

        Map<Long, ProblemSubmission> latestWrongSubmissionByProblemId = new LinkedHashMap<>();
        wrongSubmissions.forEach(submission -> latestWrongSubmissionByProblemId.putIfAbsent(
                submission.getProblem().getProblemId(),
                submission
        ));

        Map<Long, WrongNote> wrongNoteByProblemId = wrongNoteRepository
                .findAllByUserAndProblemIn(
                        user,
                        latestWrongSubmissionByProblemId.values().stream()
                                .map(ProblemSubmission::getProblem)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        wrongNote -> wrongNote.getProblem().getProblemId(),
                        Function.identity()
                ));

        List<ProblemSubmission> wrongNoteSubmissions = wrongSubmissions.stream()
                .filter(submission -> wrongNoteByProblemId.containsKey(submission.getProblem().getProblemId()))
                .toList();

        List<LocalDate> wrongNoteDates = wrongNoteSubmissions.stream()
                .map(submission -> submission.getCreatedAt().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        List<WrongNoteSummaryResponseDto> summaries = latestWrongSubmissionByProblemId.values().stream()
                .filter(submission -> wrongNoteByProblemId.containsKey(submission.getProblem().getProblemId()))
                .map(submission -> WrongNoteSummaryResponseDto.from(
                        wrongNoteByProblemId.get(submission.getProblem().getProblemId()),
                        createProblemTitle(submission.getProblem()),
                        submission.getCreatedAt().toLocalDate(),
                        submission.getSubmissionId()
                ))
                .toList();

        return WrongNoteListResponseDto.of(targetYearMonth, wrongNoteDates, summaries);
    }

    /**
     * 오답노트 문제 풀이 조회
     *
     * * 본인의 현재 학습 언어에 해당하는 오답 문제만 조회한다.
     * * 마지막으로 틀린 선택지와 정답, 해설을 함께 반환한다.
     *
     * @param loginId 로그인 ID
     * @param problemId 조회할 문제 ID
     * @return 오답 문제 풀이 정보
     * @throws BusinessException 유저, 문제, 오답노트 또는 오답 제출 이력이 없는 경우
     */
    public WrongNoteSolutionResponseDto findSolution(String loginId, Long problemId, Long wrongSubmissionId) {
        User user = findUserByLoginId(loginId);
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        if (!problem.belongsTo(resolveLearningLanguage(user))) {
            throw new BusinessException(ErrorCode.PROBLEM_NOT_FOUND);
        }

        WrongNote wrongNote = wrongNoteRepository.findByUserAndProblem(user, problem)
                .orElseThrow(() -> new BusinessException(ErrorCode.WRONG_NOTE_NOT_FOUND));
        ProblemSubmission wrongSubmission = findWrongSubmission(user, problem, wrongSubmissionId);
        List<ProblemChoice> choices = problemChoiceRepository.findAllByProblemOrderByChoiceNumberAsc(problem);

        return WrongNoteSolutionResponseDto.of(wrongNote, wrongSubmission, choices);
    }

    /**
     * 오답노트 학습 종료 시 전달받은 문제들을 복습 완료 처리합니다.
     *
     * @param loginId 로그인 ID
     * @param requestDto 복습한 오답 문제 ID 목록
     * @return 저장된 복습 완료 기록
     * @throws BusinessException 유저, 문제 또는 오답노트 기록이 없는 경우
     */
    @Transactional
    public WrongNoteReviewCompleteResponseDto completeReview(
            String loginId,
            WrongNoteReviewCompleteRequestDto requestDto
    ) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Long> problemIds = requestDto.getProblemIds().stream()
                .distinct()
                .sorted()
                .toList();
        List<Problem> problems = problemRepository.findAllById(problemIds);

        if (problems.size() != problemIds.size()) {
            throw new BusinessException(ErrorCode.PROBLEM_NOT_FOUND);
        }

        LearningLanguage learningLanguage = resolveLearningLanguage(user);
        if (problems.stream().anyMatch(problem -> !problem.belongsTo(learningLanguage))) {
            throw new BusinessException(ErrorCode.PROBLEM_NOT_FOUND);
        }

        Map<Long, WrongNote> wrongNoteByProblemId = wrongNoteRepository.findAllByUserAndProblemIn(user, problems)
                .stream()
                .collect(Collectors.toMap(
                        wrongNote -> wrongNote.getProblem().getProblemId(),
                        Function.identity()
                ));

        if (wrongNoteByProblemId.size() != problemIds.size()) {
            throw new BusinessException(ErrorCode.WRONG_NOTE_NOT_FOUND);
        }

        String reviewKey = CompletionKeyGenerator.generate(problemIds);
        WrongNoteReview review = wrongNoteReviewRepository.findByUserAndReviewKey(user, reviewKey)
                .orElse(null);

        boolean newlyCompleted = review == null;
        if (newlyCompleted) {
            review = createReview(user, reviewKey, problemIds.size());
        }

        LocalDateTime reviewedAt = LocalDateTime.now();
        problemIds.forEach(problemId -> wrongNoteByProblemId.get(problemId).completeReview(reviewedAt));

        if (newlyCompleted) {
            xpService.grantXp(user, XpActionType.WRONG_NOTE_COMPLETE);
        }

        attendanceService.save(loginId);

        return WrongNoteReviewCompleteResponseDto.from(review);
    }

    private LearningLanguage resolveLearningLanguage(User user) {
        return user.getLearningLanguage() == null
                ? LearningLanguage.JAPANESE
                : user.getLearningLanguage();
    }

    private User findUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private ProblemSubmission findWrongSubmission(User user, Problem problem, Long wrongSubmissionId) {
        if (wrongSubmissionId == null) {
            return problemSubmissionRepository
                    .findFirstByUserAndProblemAndCorrectFalseOrderBySubmissionIdDesc(user, problem)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_SUBMISSION_NOT_FOUND));
        }

        ProblemSubmission wrongSubmission = problemSubmissionRepository.findById(wrongSubmissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_SUBMISSION_NOT_FOUND));

        if (!wrongSubmission.getUser().getUserId().equals(user.getUserId())
                || !wrongSubmission.getProblem().getProblemId().equals(problem.getProblemId())
                || wrongSubmission.isCorrect()) {
            throw new BusinessException(ErrorCode.PROBLEM_SUBMISSION_NOT_FOUND);
        }

        return wrongSubmission;
    }

    private String createProblemTitle(Problem problem) {
        String levelLabel = problem.resolveLanguage().equals("EN")
                ? "CEFR " + problem.getCefrLevel()
                : "JLPT " + problem.getLevel();

        return levelLabel + " " + toCategoryLabel(problem);
    }

    private String toCategoryLabel(Problem problem) {
        if ("EN".equals(problem.resolveLanguage())) {
            return switch (problem.getCategory()) {
                case "VOCABULARY" -> "어휘";
                case "GRAMMAR" -> "문법";
                case "READING" -> "독해";
                case "LISTENING" -> "듣기";
                case "DAILY_ENGLISH" -> "실생활 영어";
                default -> problem.getCategory();
            };
        }

        return switch (problem.getCategory()) {
            case "READING" -> "독해";
            case "GRAMMAR" -> "문법";
            case "LANGUAGE_KNOWLEDGE" -> "GRAMMAR_PATTERN".equals(problem.getSubType()) ? "문법" : "문자/어휘";
            default -> problem.getCategory();
        };
    }

    private WrongNoteReview createReview(User user, String reviewKey, int problemCount) {
        return wrongNoteReviewRepository.save(WrongNoteReview.builder()
                .user(user)
                .reviewKey(reviewKey)
                .problemCount(problemCount)
                .build());
    }
}
