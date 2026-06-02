package com.qurve.challenge.repository;

import com.qurve.challenge.domain.ChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, Long> {
    List<ChallengeProgress> findAllByChallenge_ChallengeIdIn(Collection<Long> challengeIds);
}
