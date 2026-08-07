package com.qurve.challenge.repository;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findAllByUser_LoginId(String loginId);
    Optional<Challenge> findByUserAndGoalType(User user, ChallengeGoalType goalType);
    long countByUser(User user);
    long countByUserAndStatus(User user, ChallengeStatus status);
}
