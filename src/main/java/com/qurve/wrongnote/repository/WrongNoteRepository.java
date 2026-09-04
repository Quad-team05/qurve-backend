package com.qurve.wrongnote.repository;

import com.qurve.problem.domain.Problem;
import com.qurve.user.domain.User;
import com.qurve.wrongnote.domain.WrongNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long> {
    Optional<WrongNote> findByUserAndProblem(User user, Problem problem);

    List<WrongNote> findAllByUserAndProblemIn(User user, Collection<Problem> problems);

    long countByUser(User user);
}
