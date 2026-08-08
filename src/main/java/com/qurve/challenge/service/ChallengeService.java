package com.qurve.challenge.service;

import com.qurve.badge.service.BadgeService;
import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeProgress;
import com.qurve.challenge.dto.request.ChallengeCreateRequestDto;
import com.qurve.challenge.dto.response.ChallengeCreateResponseDto;
import com.qurve.challenge.dto.response.ChallengeGoalTypeResponseDto;
import com.qurve.challenge.dto.response.ChallengeMainResponseDto;
import com.qurve.challenge.dto.response.ChallengeManageResponseDto;
import com.qurve.challenge.repository.ChallengeProgressRepository;
import com.qurve.challenge.repository.ChallengeRepository;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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
    private final BadgeService badgeService;

    public List<ChallengeGoalTypeResponseDto> getGoalTypes() {
        return Arrays.stream(ChallengeGoalType.values())
                .map(ChallengeGoalTypeResponseDto::from)
                .toList();
    }

    public List<ChallengeManageResponseDto> getMyChallenges(String loginId) {
        return challengeRepository.findAllByUser_LoginId(loginId)
                .stream()
                .map(ChallengeManageResponseDto::from)
                .toList();
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
        List<Challenge> challenges = challengeRepository.findAllByUser_LoginId(loginId);

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

        Challenge challenge = requestDto.toEntity(user);
        Challenge savedChallenge = challengeRepository.save(challenge);
        badgeService.evaluate(user);

        return ChallengeCreateResponseDto.from(savedChallenge);
    }

    private int calculateProgressRate(int targetValue, int completedDays) {
        if (targetValue <= 0) {
            return 0;
        }

        int progressRate = (int) Math.round((completedDays * 100.0) / targetValue);
        return Math.min(progressRate, 100);
    }
}
