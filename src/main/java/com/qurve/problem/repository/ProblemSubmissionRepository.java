package com.qurve.problem.repository;

import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemSubmission;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProblemSubmissionRepository extends JpaRepository<ProblemSubmission, Long> {
    Optional<ProblemSubmission> findFirstByUserAndProblemOrderBySubmissionIdDesc(User user, Problem problem);
    Optional<ProblemSubmission> findFirstByUserAndProblemAndCorrectFalseOrderBySubmissionIdDesc(User user, Problem problem);
    List<ProblemSubmission> findAllByUserAndProblemOrderBySubmissionIdDesc(User user, Problem problem);
    List<ProblemSubmission> findAllByUserAndCreatedAtBetween(User user, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("""
            select ps
            from ProblemSubmission ps
            join fetch ps.problem
            where ps.user = :user
              and ps.correct = false
              and ps.createdAt >= :startDateTime
              and ps.createdAt < :endDateTime
            order by ps.createdAt desc, ps.submissionId desc
            """)
    List<ProblemSubmission> findAllWrongByUserAndCreatedAtBetween(
            @Param("user") User user,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    long countByUser(User user);
    long countByUserAndCorrectTrue(User user);
}
