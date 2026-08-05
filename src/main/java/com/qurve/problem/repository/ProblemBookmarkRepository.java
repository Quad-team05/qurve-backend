package com.qurve.problem.repository;

import com.qurve.problem.domain.Problem;
import com.qurve.problem.domain.ProblemBookmark;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemBookmarkRepository extends JpaRepository<ProblemBookmark, Long> {
    boolean existsByUserAndProblem(User user, Problem problem);
    Optional<ProblemBookmark> findByUserAndProblem(User user, Problem problem);
    List<ProblemBookmark> findByUserOrderByCreatedAtDesc(User user);
}
