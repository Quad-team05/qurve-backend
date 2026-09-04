package com.qurve.challenge.service;

import com.qurve.badge.service.BadgeService;
import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeProgress;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.challenge.dto.request.ChallengeCreateRequestDto;
import com.qurve.challenge.dto.request.ChallengeUpdateRequestDto;
import com.qurve.challenge.dto.response.ChallengeCreateResponseDto;
import com.qurve.challenge.dto.response.ChallengeMainResponseDto;
import com.qurve.challenge.dto.response.ChallengeManageResponseDto;
import com.qurve.challenge.dto.response.ChallengeManagementResponseDto;
import com.qurve.challenge.dto.response.ChallengeUpdateResponseDto;
import com.qurve.attendance.domain.StudyStatistics;
import com.qurve.attendance.repository.StudyStatisticsRepository;
import com.qurve.challenge.repository.ChallengeProgressRepository;
import com.qurve.challenge.repository.ChallengeRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 챌린지 관리 화면의 전체 현황과 상태별 챌린지 목록을 조회합니다.
     *
     * @param loginId 로그인 ID
     * @return 연속 학습일, 전체 달성률, 진행 중 및 완료 챌린지 목록
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public ChallengeManagementResponseDto findManagement(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<Challenge> challenges = challengeRepository.findAllByUser_LoginId(loginId);

        List<ChallengeManageResponseDto> challengeResponses = challenges.stream()
                .map(challenge -> ChallengeManageResponseDto.from(
                        challenge,
                        calculateProgressRate(challenge.getTargetValue(), challenge.getCurrentValue())
                ))
                .toList();

        List<ChallengeManageResponseDto> activeChallenges = challengeResponses.stream()
                .filter(challenge -> challenge.getStatus() == com.qurve.challenge.domain.ChallengeStatus.ACTIVE)
                .toList();
        List<ChallengeManageResponseDto> completedChallenges = challengeResponses.stream()
                .filter(challenge -> challenge.getStatus() == com.qurve.challenge.domain.ChallengeStatus.COMPLETED)
                .toList();

        int totalProgressRate = activeChallenges.isEmpty()
                ? 0
                : (int) Math.round(activeChallenges.stream()
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
     * 메인페이지 챌린지 조회
     *
     * * 사용자의 챌린지 목록과 진행 데이터를 조회하고,
     * 목표값 대비 달성률을 계산해 반환한다(progressRate)
     *
     * @param loginId 로그인 ID
     * @return 메인페이지 챌린지 응답 목록
     */
    public List<ChallengeMainResponseDto> findAllForMain(String loginId) {
        List<Challenge> challenges = challengeRepository.findAllByUser_LoginId(loginId)
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
                .collect(Collectors.toMap(
                        progress -> progress.getChallenge().getChallengeId(),
                        Function.identity()
                ));

        return challenges.stream()
                .map(challenge -> {
                    ChallengeProgress progress = progressByChallengeId.get(challenge.getChallengeId());

                    int completedDays = progress == null ? 0 : progress.getCompletedDays();
                    int progressRate = calculateProgressRate(challenge.getTargetValue(), completedDays);

                    return ChallengeMainResponseDto.from(challenge, completedDays, progressRate);
                })
                .toList();
    }

    @Transactional
    public ChallengeCreateResponseDto createChallenge(
            ChallengeCreateRequestDto requestDto,
            String loginId
    ) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (requestDto.getEndDate().isBefore(requestDto.getStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_CHALLENGE_PERIOD);
        }

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
     * 진행 중인 챌린지의 제목, 목표값, 기간을 수정합니다.
     *
     * * 목표 유형은 기존 활동 이력의 기준이므로 수정 대상에서 제외합니다.
     *
     * @param challengeId 수정할 챌린지 ID
     * @param requestDto 챌린지 수정 정보
     * @param loginId 로그인 ID
     * @return 수정된 챌린지 정보
     * @throws BusinessException 챌린지가 없거나 수정할 수 없는 상태인 경우
     */
    @Transactional
    public ChallengeUpdateResponseDto update(
            Long challengeId,
            ChallengeUpdateRequestDto requestDto,
            String loginId
    ) {
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
     * 로그인한 사용자의 챌린지와 연결된 진행도 정보를 삭제합니다.
     *
     * @param challengeId 삭제할 챌린지 ID
     * @param loginId 로그인 ID
     * @throws BusinessException 챌린지가 존재하지 않는 경우
     */
    @Transactional
    public void delete(Long challengeId, String loginId) {
        User user = findUserByLoginId(loginId);
        Challenge challenge = findChallengeByIdAndUser(challengeId, user);

        challengeProgressRepository.findByChallenge(challenge)
                .ifPresent(challengeProgressRepository::delete);
        challengeRepository.delete(challenge);
    }

    private User findUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Challenge findChallengeByIdAndUser(Long challengeId, User user) {
        return challengeRepository.findByChallengeIdAndUser(challengeId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
    }

    private void validateChallengePeriod(java.time.LocalDate startDate, java.time.LocalDate endDate) {
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
