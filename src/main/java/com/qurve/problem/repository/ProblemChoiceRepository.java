package com.qurve.problem.repository;

import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProblemChoiceRepository extends JpaRepository<ProblemChoice, Long> {
    long countByProblem(Problem problem);
    List<ProblemChoice> findAllByProblemOrderByChoiceNumberAsc(Problem problem);

    @Query("""
            select pc
            from ProblemChoice pc
            where pc.problem in :problems
            order by pc.problem.problemId asc, pc.choiceNumber asc
            """)
    List<ProblemChoice> findAllByProblemsOrderByProblemIdAscChoiceNumberAsc(@Param("problems") List<Problem> problems);
}
