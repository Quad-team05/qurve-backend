package com.qurve.problem.repository;

import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemSubmission;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProblemSubmissionRepository extends JpaRepository<ProblemSubmission, Long> {
    Optional<ProblemSubmission> findFirstByUserAndProblemOrderBySubmissionIdDesc(User user, Problem problem);
    List<ProblemSubmission> findAllByUserAndProblemOrderBySubmissionIdDesc(User user, Problem problem);
    List<ProblemSubmission> findAllByUserAndCreatedAtBetween(User user, LocalDateTime startDateTime, LocalDateTime endDateTime);
    long countByUser(User user);
    long countByUserAndCorrectTrue(User user);
}
