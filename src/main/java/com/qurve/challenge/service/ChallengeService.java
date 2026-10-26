package com.qurve.challenge.service;

import com.qurve.attendance.domain.StudyStatistics;
import com.qurve.attendance.repository.StudyStatisticsRepository;
import com.qurve.badge.service.BadgeService;
import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeProgress;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.challenge.dto.request.ChallengeCreateRequestDto;
import com.qurve.challenge.dto.request.ChallengeUpdateRequestDto;
import com.qurve.challenge.dto.response.ChallengeCreateResponseDto;
import com.qurve.challenge.dto.response.ChallengeMainResponseDto;
import com.qurve.challenge.dto.response.ChallengeManageResponseDto;
import com.qurve.challenge.dto.response.ChallengeManagementResponseDto;
import com.qurve.challenge.dto.response.ChallengeUpdateResponseDto;
import com.qurve.challenge.repository.ChallengeProgressRepository;
import com.qurve.challenge.repository.ChallengeRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final UserRepository userRepository;
    private final StudyStatisticsRepository studyStatisticsRepository;
    private final BadgeService badgeService;

    /**
     * 현재 학습 언어의 챌린지 관리 현황을 조회합니다.
     *
     * @param loginId 로그인 ID
     * @return 연속 학습일, 전체 달성률, 진행 중 및 완료 챌린지 목록
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public ChallengeManagementResponseDto findManagement(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<Challenge> challenges = findChallengesByCurrentLanguage(user);

        List<ChallengeManageResponseDto> challengeResponses = challenges.stream()
                .map(challenge -> ChallengeManageResponseDto.from(
                        challenge, calculateProgressRate(challenge.getTargetValue(), challenge.getCurrentValue())))
                .toList();

        List<ChallengeManageResponseDto> activeChallenges = challengeResponses.stream()
                .filter(challenge -> challenge.getStatus() == ChallengeStatus.ACTIVE)
                .toList();

        List<ChallengeManageResponseDto> completedChallenges = challengeResponses.stream()
                .filter(challenge -> challenge.getStatus() == ChallengeStatus.COMPLETED)
                .toList();

        int totalProgressRate = activeChallenges.isEmpty() ? 0 : (int) Math.round(activeChallenges.stream()
                .mapToInt(ChallengeManageResponseDto::getProgressRate)
                .average()
                .orElse(0));

        int streakDays = studyStatisticsRepository.findByUser(user)
                .map(StudyStatistics::getStreakDays)
                .orElse(0);

        return ChallengeManagementResponseDto.builder()
                .streakDays(streakDays)
                .totalProgressRate(totalProgressRate)
                .activeChallengeCount(activeChallenges.size())
                .completedChallengeCount(completedChallenges.size())
                .activeChallenges(activeChallenges)
                .completedChallenges(completedChallenges)
                .build();
    }

    /**
     * 현재 학습 언어의 메인페이지 챌린지를 조회합니다.
     *
     * @param loginId 로그인 ID
     * @return 진행 중인 챌린지와 진행률
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public List<ChallengeMainResponseDto> findAllForMain(String loginId) {
        User user = findUserByLoginId(loginId);
        List<Challenge> challenges = findChallengesByCurrentLanguage(user)
                .stream()
                .filter(challenge -> challenge.getStatus() == com.qurve.challenge.domain.ChallengeStatus.ACTIVE)
                .toList();

        if (challenges.isEmpty()) {
            return List.of();
        }

        List<Long> challengeIds = challenges.stream()
                .map(Challenge::getChallengeId)
                .toList();

        Map<Long, ChallengeProgress> progressByChallengeId = challengeProgressRepository
                .findAllByChallenge_ChallengeIdIn(challengeIds)
                .stream()
                .collect(Collectors.toMap(progress -> progress.getChallenge().getChallengeId(), Function.identity()));

        return challenges.stream()
                .map(challenge -> {ChallengeProgress progress = progressByChallengeId.get(challenge.getChallengeId());

                    int completedDays = progress == null ? 0 : progress.getCompletedDays();

                    int progressRate = calculateProgressRate(challenge.getTargetValue(), completedDays);

                    return ChallengeMainResponseDto.from(challenge, completedDays, progressRate);
                })
                .toList();
    }

    /**
     * 현재 학습 언어의 챌린지를 생성합니다.
     *
     * ChallengeCreateRequestDto.toEntity(user)에서
     * 사용자의 현재 학습 언어를 챌린지에 저장합니다.
     */
    @Transactional
    public ChallengeCreateResponseDto createChallenge(ChallengeCreateRequestDto requestDto, String loginId) {
        User user = findUserByLoginId(loginId);

        validateChallengePeriod(requestDto.getStartDate(), requestDto.getEndDate());

        Challenge challenge = requestDto.toEntity(user);
        Challenge savedChallenge = challengeRepository.save(challenge);

        challengeProgressRepository.save(ChallengeProgress.builder()
                .challenge(savedChallenge)
                .completedDays(0)
                .build());

        badgeService.evaluate(user);

        return ChallengeCreateResponseDto.from(savedChallenge);
    }

    /**
     * 현재 학습 언어에 속한 진행 중 챌린지를 수정합니다.
     *
     * 목표 유형과 학습 언어는 변경하지 않습니다.
     *
     * @param challengeId 수정할 챌린지 ID
     * @param requestDto 챌린지 수정 정보
     * @param loginId 로그인 ID
     * @return 수정된 챌린지 정보
     * @throws BusinessException 챌린지가 없거나 수정할 수 없는 상태인 경우
     */
    @Transactional
    public ChallengeUpdateResponseDto update(Long challengeId, ChallengeUpdateRequestDto requestDto, String loginId) {
        User user = findUserByLoginId(loginId);
        Challenge challenge = findChallengeByIdAndUser(challengeId, user);

        if (challenge.getStatus() != ChallengeStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CHALLENGE_NOT_EDITABLE);
        }

        validateChallengePeriod(requestDto.getStartDate(), requestDto.getEndDate());

        challenge.update(
                requestDto.getTitle(),
                requestDto.getTargetValue(),
                requestDto.getStartDate(),
                requestDto.getEndDate()
        );

        challengeProgressRepository.findByChallenge(challenge)
                .ifPresent(progress -> progress.updateCompletedDays(challenge.getCurrentValue()));

        return ChallengeUpdateResponseDto.from(challenge);
    }

    /**
     * 현재 학습 언어에 속한 챌린지와 연결된 진행도 정보를 삭제합니다.
     *
     * @param challengeId 삭제할 챌린지 ID
     * @param loginId 로그인 ID
     * @throws BusinessException 챌린지가 존재하지 않는 경우
     */
    @Transactional
    public void delete(Long challengeId, String loginId) {
        User user = findUserByLoginId(loginId);
        Challenge challenge = findChallengeByIdAndUser(challengeId, user);

        challengeProgressRepository.findByChallenge(challenge).ifPresent(challengeProgressRepository::delete);

        challengeRepository.delete(challenge);
    }

    private User findUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 사용자 소유 여부와 현재 학습 언어를 함께 검증합니다.
     */
    private Challenge findChallengeByIdAndUser(Long challengeId, User user) {
        LearningLanguage language = resolveLearningLanguage(user);

        return challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .filter(challenge -> {
                    LearningLanguage challengeLanguage =
                            challenge.getLearningLanguage() == null
                                    ? LearningLanguage.JAPANESE
                                    : challenge.getLearningLanguage();

                    return challengeLanguage == language;
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND)
                );
    }

    /** 학습 언어가 없는 기존 사용자는 일본어로 처리합니다. */
    private LearningLanguage resolveLearningLanguage(User user) {
        return user.getLearningLanguage() == null
                ? LearningLanguage.JAPANESE
                : user.getLearningLanguage();
    }

    private List<Challenge> findChallengesByCurrentLanguage(User user) {
        return challengeRepository.findAllByUserAndLearningLanguage(
                user,
                resolveLearningLanguage(user),
                LearningLanguage.JAPANESE
        );
    }

    private void validateChallengePeriod(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_CHALLENGE_PERIOD);
        }
    }

    private int calculateProgressRate(int targetValue, int completedDays) {
        if (targetValue <= 0) {
            return 0;
        }

        int progressRate = (int) Math.round((completedDays * 100.0) / targetValue);

        return Math.min(progressRate, 100);
    }
}