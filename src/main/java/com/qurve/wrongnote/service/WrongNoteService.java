package com.qurve.wrongnote.service;

import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.XpActionType;
import com.qurve.global.exception.BusinessException;
import com.qurve.global.util.CompletionKeyGenerator;
import com.qurve.problem.domain.Problem;
import com.qurve.problem.repository.ProblemRepository;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.xp.service.XpService;
import com.qurve.wrongnote.domain.WrongNote;
import com.qurve.wrongnote.domain.WrongNoteReview;
import com.qurve.wrongnote.dto.request.WrongNoteReviewCompleteRequestDto;
import com.qurve.wrongnote.dto.response.WrongNoteReviewCompleteResponseDto;
import com.qurve.wrongnote.repository.WrongNoteRepository;
import com.qurve.wrongnote.repository.WrongNoteReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final WrongNoteRepository wrongNoteRepository;
    private final WrongNoteReviewRepository wrongNoteReviewRepository;
    private final XpService xpService;

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

        return WrongNoteReviewCompleteResponseDto.from(review);
    }

    private WrongNoteReview createReview(User user, String reviewKey, int problemCount) {
        return wrongNoteReviewRepository.save(WrongNoteReview.builder()
                .user(user)
                .reviewKey(reviewKey)
                .problemCount(problemCount)
                .build());
    }
}
