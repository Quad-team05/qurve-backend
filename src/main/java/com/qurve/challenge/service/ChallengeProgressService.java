package com.qurve.challenge.service;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeProgress;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.challenge.repository.ChallengeProgressRepository;
import com.qurve.challenge.repository.ChallengeRepository;
import com.qurve.global.enums.XpActionType;
import com.qurve.user.domain.User;
import com.qurve.xp.service.XpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 학습 활동 발생 시 연결된 챌린지 진행도를 갱신합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeProgressService {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final XpService xpService;

    /**
     * 활동 유형에 해당하는 진행 중 챌린지에 진행도를 누적합니다.
     *
     * @param user 활동을 수행한 사용자
     * @param goalType 누적할 챌린지 목표 유형
     * @param amount 증가시킬 달성값
     */
    @Transactional
    public void addProgress(User user, ChallengeGoalType goalType, int amount) {
        if (amount <= 0) {
            return;
        }

        LocalDate today = LocalDate.now(KST_ZONE);
        challengeRepository.findAllByUserAndGoalTypeAndStatus(user, goalType, ChallengeStatus.ACTIVE)
                .stream()
                .filter(challenge -> challenge.isActiveOn(today))
                .forEach(challenge -> updateProgress(challenge, amount));
    }

    private void updateProgress(Challenge challenge, int amount) {
        boolean wasActive = challenge.getStatus() == ChallengeStatus.ACTIVE;
        challenge.addProgress(amount);

        ChallengeProgress progress = challengeProgressRepository.findByChallenge(challenge)
                .orElseGet(() -> ChallengeProgress.builder()
                        .challenge(challenge)
                        .completedDays(0)
                        .build());

        progress.updateCompletedDays(challenge.getCurrentValue());
        challengeProgressRepository.save(progress);

        if (wasActive && challenge.getStatus() == ChallengeStatus.COMPLETED) {
            xpService.grantXp(challenge.getUser(), XpActionType.CHALLENGE_COMPLETE);
        }
    }
}
