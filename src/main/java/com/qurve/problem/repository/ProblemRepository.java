package com.qurve.problem.repository;

import com.qurve.problem.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    interface TodayLearningSetProjection {
        String getCategory();
        String getSubType();
        Long getProblemCount();
    }

    Optional<Problem> findByLevelAndCategoryAndSubTypeAndQuestionFormatAndQuestionText(
            String level,
            String category,
            String subType,
            String questionFormat,
            String questionText
    );

    @Query("""
            select p
            from Problem p
            where p.isActive = true
              and (:language is null or p.language = :language)
              and (:cefrLevel is null or p.cefrLevel = :cefrLevel)
              and (:level is null or p.level = :level)
              and (:usageType is null or p.usageType = :usageType)
              and (:category is null or p.category = :category)
              and (:subType is null or p.subType = :subType)
              and (:topic is null or p.topic = :topic)
            order by p.problemId asc
            """)
    List<Problem> findAllByConditionsOrderByProblemIdAsc(
            @Param("language") String language,
            @Param("cefrLevel") String cefrLevel,
            @Param("level") String level,
            @Param("usageType") String usageType,
            @Param("category") String category,
            @Param("subType") String subType,
            @Param("topic") String topic
    );

    @Query("""
            select p.category as category,
                   p.subType as subType,
                   count(p) as problemCount
            from Problem p
            where p.level = :level
              and p.isActive = true
            group by p.category, p.subType
            order by p.category asc, p.subType asc
            """)
    List<TodayLearningSetProjection> findTodayLearningSetsByLevel(String level);
}
