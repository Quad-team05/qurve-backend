package com.qurve.problem.repository;

import com.qurve.problem.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
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
}
