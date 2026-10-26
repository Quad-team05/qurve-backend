package com.qurve.challenge.repository;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, Long> {
    List<ChallengeProgress> findAllByChallenge_ChallengeIdIn(Collection<Long> challengeIds);
    Optional<ChallengeProgress> findByChallenge(Challenge challenge);
}
