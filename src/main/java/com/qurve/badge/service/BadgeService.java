package com.qurve.badge.service;

import com.qurve.attendance.domain.StudyStatistics;
import com.qurve.attendance.repository.StudyStatisticsRepository;
import com.qurve.badge.domain.BadgeDefinition;
import com.qurve.badge.domain.UserBadge;
import com.qurve.badge.dto.response.BadgeListResponseDto;
import com.qurve.badge.dto.response.BadgeResponseDto;
import com.qurve.badge.repository.UserBadgeRepository;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.challenge.repository.ChallengeRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.problem.repository.ProblemSubmissionRepository;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.vocabulary.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final StudyStatisticsRepository studyStatisticsRepository;
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ChallengeRepository challengeRepository;

    /**
     * 전체 배지 조회
     *
     * * 전체 배지 목록과 로그인한 사용자의 획득 여부 및 진행률을 함께 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 전체 배지와 유저별 획득 상태
     */
    public BadgeListResponseDto findAll(String loginId) {
        User user = findUserByLoginId(loginId);
        BadgeComputation badgeComputation = loadBadgeComputation(user);
        return createBadgeListResponse(badgeComputation.userBadgeMap(), badgeComputation.badgeProgress());
    }

    /**
     * 배지 달성 조건 평가
     *
     * * 현재 저장된 학습 데이터를 기준으로 달성 가능한 배지를 지급한다.
     *
     * @param loginId 로그인 ID
     */
    @Transactional
    public void evaluate(String loginId) {
        User user = findUserByLoginId(loginId);
        evaluate(user);
    }

    /**
     * 배지 달성 조건 평가 후 전체 배지 조회
     *
     * * 한 번 계산한 진행률과 획득 상태를 재사용해
     * 배지 지급과 응답 생성을 함께 처리한다.
     *
     * @param loginId 로그인 ID
     * @return 평가 반영 후 전체 배지 목록
     */
    @Transactional
    public BadgeListResponseDto evaluateAndFindAll(String loginId) {
        User user = findUserByLoginId(loginId);
        return evaluateAndFindAll(user);
    }

    /**
     * 배지 달성 조건 평가
     *
     * * 출석, 문제 제출, 단어 북마크, 챌린지 생성 등 사용자 행동 이후 호출해
     * 새로 달성한 배지를 저장한다.
     *
     * @param user 평가 대상 유저
     */
    @Transactional
    public void evaluate(User user) {
        BadgeComputation badgeComputation = loadBadgeComputation(user);
        saveAchievedBadges(user, badgeComputation.userBadgeMap(), badgeComputation.badgeProgress());
    }

    private User findUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private BadgeListResponseDto evaluateAndFindAll(User user) {
        BadgeComputation badgeComputation = loadBadgeComputation(user);
        Map<BadgeDefinition, UserBadge> userBadgeMap = badgeComputation.userBadgeMap();
        BadgeProgress badgeProgress = badgeComputation.badgeProgress();

        saveAchievedBadges(user, userBadgeMap, badgeProgress);

        return createBadgeListResponse(userBadgeMap, badgeProgress);
    }

    private BadgeComputation loadBadgeComputation(User user) {
        Map<BadgeDefinition, UserBadge> userBadgeMap = userBadgeRepository.findAllByUser(user)
                .stream()
                .collect(Collectors.toMap(UserBadge::getBadgeDefinition, Function.identity()));
        BadgeProgress badgeProgress = calculateProgress(user);

        return new BadgeComputation(userBadgeMap, badgeProgress);
    }

    private BadgeProgress calculateProgress(User user) {
        StudyStatistics studyStatistics = studyStatisticsRepository.findByUser_UserId(user.getUserId())
                .orElse(null);

        int attendanceStreak = studyStatistics == null ? 0 : studyStatistics.getStreakDays();
        int totalStudyTime = studyStatistics == null ? 0 : studyStatistics.getTotalStudyTime();
        int problemSubmissionCount = (int) problemSubmissionRepository.countByUser(user);
        int correctProblemCount = (int) problemSubmissionRepository.countByUserAndCorrectTrue(user);
        int accuracyRate = problemSubmissionCount == 0
                ? 0
                : (int) Math.floor(correctProblemCount * 100.0 / problemSubmissionCount);
        int wordBookmarkCount = (int) bookmarkRepository.countByUser(user);
        int challengeCreatedCount = (int) challengeRepository.countByUser(user);
        int challengeCompletedCount = (int) challengeRepository.countByUserAndStatus(user, ChallengeStatus.COMPLETED);

        Map<BadgeDefinition, Integer> currentValues = new EnumMap<>(BadgeDefinition.class);
        for (BadgeDefinition badgeDefinition : BadgeDefinition.orderedValues()) {
            currentValues.put(badgeDefinition, switch (badgeDefinition.getConditionType()) {
                case FIRST_LOGIN -> 1;
                case ATTENDANCE_STREAK -> attendanceStreak;
                case PROBLEM_SUBMISSION_COUNT -> problemSubmissionCount;
                case CORRECT_PROBLEM_COUNT -> correctProblemCount;
                case ACCURACY_RATE -> accuracyRate;
                case WORD_BOOKMARK_COUNT -> wordBookmarkCount;
                case CHALLENGE_CREATED_COUNT -> challengeCreatedCount;
                case CHALLENGE_COMPLETED_COUNT -> challengeCompletedCount;
                case TOTAL_STUDY_TIME -> totalStudyTime;
                default -> 0;
            });
        }

        return new BadgeProgress(currentValues);
    }

    private void saveAchievedBadges(
            User user,
            Map<BadgeDefinition, UserBadge> userBadgeMap,
            BadgeProgress badgeProgress
    ) {
        LocalDateTime now = LocalDateTime.now();

        List<UserBadge> newUserBadges = BadgeDefinition.orderedValues().stream()
                .filter(badgeDefinition -> badgeProgress.isAchieved(badgeDefinition))
                .filter(badgeDefinition -> !userBadgeMap.containsKey(badgeDefinition))
                .map(badgeDefinition -> UserBadge.builder()
                        .user(user)
                        .badgeDefinition(badgeDefinition)
                        .achievedAt(now)
                        .build())
                .toList();

        if (newUserBadges.isEmpty()) {
            return;
        }

        userBadgeRepository.saveAll(newUserBadges);
        newUserBadges.forEach(userBadge -> userBadgeMap.put(userBadge.getBadgeDefinition(), userBadge));
    }

    private BadgeListResponseDto createBadgeListResponse(
            Map<BadgeDefinition, UserBadge> userBadgeMap,
            BadgeProgress badgeProgress
    ) {
        List<BadgeResponseDto> badges = BadgeDefinition.orderedValues().stream()
                .map(badgeDefinition -> BadgeResponseDto.of(
                        badgeDefinition,
                        userBadgeMap.get(badgeDefinition),
                        badgeProgress.currentValueOf(badgeDefinition)
                ))
                .toList();

        return BadgeListResponseDto.from(badges);
    }

    private record BadgeComputation(
            Map<BadgeDefinition, UserBadge> userBadgeMap,
            BadgeProgress badgeProgress
    ) {
    }

    private record BadgeProgress(Map<BadgeDefinition, Integer> currentValues) {

        private int currentValueOf(BadgeDefinition badgeDefinition) {
            return currentValues.getOrDefault(badgeDefinition, 0);
        }

        private boolean isAchieved(BadgeDefinition badgeDefinition) {
            return currentValueOf(badgeDefinition) >= badgeDefinition.getTargetValue();
        }
    }
}
