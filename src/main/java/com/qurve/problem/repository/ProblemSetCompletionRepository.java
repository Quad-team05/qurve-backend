package com.qurve.problem.repository;

import com.qurve.problem.domain.ProblemSetCompletion;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProblemSetCompletionRepository extends JpaRepository<ProblemSetCompletion, Long> {
    Optional<ProblemSetCompletion> findByUserAndSetKey(User user, String setKey);
}
