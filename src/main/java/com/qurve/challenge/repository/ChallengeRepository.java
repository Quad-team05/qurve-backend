package com.qurve.challenge.repository;

import com.qurve.challenge.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findAllByUser_LoginId(String loginId);
}