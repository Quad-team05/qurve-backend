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
    /**
     * 학습 언어별 유닛 진행 기록 조회
     *
     * * 사용자의 학습 언어·레벨·유닛에 해당하는 진행 기록을 조회한다.
     * * 일본어 조회 시 학습 언어가 null인 기존 기록도 포함한다.
     */
    @Query("""
        select up
        from UnitProgress up
        where up.user = :user
          and up.level = :level
          and up.unitNumber = :unitNumber
          and (
              up.learningLanguage = :language
              or (
                  :language = :legacyLanguage
                  and up.learningLanguage is null
              )
          )
        """)
    Optional<UnitProgress> findProgressByLanguageAndLevelAndUnitNumber(
            @Param("user") User user,
            @Param("language") LearningLanguage language,
            @Param("legacyLanguage") LearningLanguage legacyLanguage,
            @Param("level") String level,
            @Param("unitNumber") Integer unitNumber
    );
    Optional<UnitProgress> findFirstByUserAndStatusOrderByUpdatedAtDesc(User user, UnitStatus status);
    /**
     * 학습 언어별 유닛 진행 상태 목록 조회
     *
     * * 사용자의 학습 언어와 레벨에 해당하는 유닛 진행 상태를 조회한다.
     * * language가 legacyLanguage와 같으면 학습 언어가 null인 기존 기록도 포함한다.
     *
     * @param legacyLanguage 언어가 null인 기존 기록에 적용할 언어
     */
    @Query("""
        select up
        from UnitProgress up
        where up.user = :user
          and up.level = :level
          and (
              up.learningLanguage = :language
              or (
                  :language = :legacyLanguage
                  and up.learningLanguage is null
              )
          )
        order by up.unitNumber asc
        """)
    List<UnitProgress> findProgressByLanguageAndLevel(
            @Param("user") User user,
            @Param("language") LearningLanguage language,
            @Param("legacyLanguage") LearningLanguage legacyLanguage,
            @Param("level") String level
    );
}
