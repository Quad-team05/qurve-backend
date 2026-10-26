package com.qurve.challenge.repository;

import com.qurve.challenge.domain.Challenge;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.domain.ChallengeStatus;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findAllByUser_LoginId(String loginId);
    List<Challenge> findAllByUserAndGoalTypeAndStatus(User user, ChallengeGoalType goalType, ChallengeStatus status);
    Optional<Challenge> findByUserAndGoalType(User user, ChallengeGoalType goalType);
    Optional<Challenge> findByChallengeIdAndUser(Long challengeId, User user);
    Optional<Challenge> findFirstByUserAndGoalTypeAndStatusOrderByCreatedAtDesc(
            User user,
            ChallengeGoalType goalType,
            ChallengeStatus status
    );
    Optional<Challenge> findFirstByUserAndGoalTypeAndStatusAndLearningLanguageOrderByCreatedAtDesc(
            User user,
            ChallengeGoalType goalType,
            ChallengeStatus status,
            LearningLanguage learningLanguage
    );
    long countByUser(User user);
    long countByUserAndStatus(User user, ChallengeStatus status);

    @Query("""
            select c
            from Challenge c
            where c.user = :user
              and (c.learningLanguage = :learningLanguage
                   or (:learningLanguage = :legacyLanguage and c.learningLanguage is null))
            order by c.createdAt desc
            """)
    List<Challenge> findAllByUserAndLearningLanguage(
            @Param("user") User user,
            @Param("learningLanguage") LearningLanguage learningLanguage,
            @Param("legacyLanguage") LearningLanguage legacyLanguage
    );

    @Query("""
            select c
            from Challenge c
            where c.user = :user
              and c.goalType = :goalType
              and c.status = :status
              and (c.learningLanguage = :learningLanguage
                   or (:learningLanguage = :legacyLanguage and c.learningLanguage is null))
            order by c.createdAt desc
            """)
    List<Challenge> findAllActiveByUserAndGoalTypeForLanguage(
            @Param("user") User user,
            @Param("goalType") ChallengeGoalType goalType,
            @Param("status") ChallengeStatus status,
            @Param("learningLanguage") LearningLanguage learningLanguage,
            @Param("legacyLanguage") LearningLanguage legacyLanguage
    );
}
