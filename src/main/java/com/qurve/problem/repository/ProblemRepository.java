package com.qurve.problem.repository;

import com.qurve.problem.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    List<Problem> findAllByLevelAndCategoryAndSubTypeOrderByProblemIdAsc(
            String level,
            String category,
            String subType
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
