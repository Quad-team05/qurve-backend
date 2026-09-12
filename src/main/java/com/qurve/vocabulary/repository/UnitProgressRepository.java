package com.qurve.vocabulary.repository;

import com.qurve.global.enums.LearningLanguage;
import com.qurve.vocabulary.domain.UnitProgress;
import com.qurve.vocabulary.enums.UnitStatus;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UnitProgressRepository extends JpaRepository<UnitProgress, Long> {
    List<UnitProgress> findByUserAndLevel(User user, String level);
    List<UnitProgress> findByUserAndLevelOrderByUnitNumberAsc(User user, String level);
    Optional<UnitProgress> findByUserAndLevelAndUnitNumber(User user, String level, Integer unitNumber);
    Optional<UnitProgress> findFirstByUserAndStatusOrderByUpdatedAtDesc(User user, UnitStatus status);

    @Query("""
            select up
            from UnitProgress up
            where up.user = :user
              and up.status = :status
              and (up.learningLanguage = :learningLanguage
                   or (:learningLanguage = :legacyLanguage and up.learningLanguage is null))
            order by up.updatedAt desc
            """)
    List<UnitProgress> findAllByUserAndStatusAndLearningLanguageOrderByUpdatedAtDesc(
            @Param("user") User user,
            @Param("status") UnitStatus status,
            @Param("learningLanguage") LearningLanguage learningLanguage,
            @Param("legacyLanguage") LearningLanguage legacyLanguage
    );
}
