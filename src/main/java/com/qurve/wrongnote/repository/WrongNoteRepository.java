package com.qurve.wrongnote.repository;

import com.qurve.problem.domain.Problem;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.user.domain.User;
import com.qurve.wrongnote.domain.WrongNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long> {
    Optional<WrongNote> findByUserAndProblem(User user, Problem problem);

    List<WrongNote> findAllByUserAndProblemIn(User user, Collection<Problem> problems);

    long countByUser(User user);

    @Query("""
            select count(w)
            from WrongNote w
            where w.user = :user
              and (w.problem.language = :language
                   or (:language = :legacyLanguage and w.problem.language is null))
            """)
    long countByUserAndProblemLanguage(
            @Param("user") User user,
            @Param("language") String language,
            @Param("legacyLanguage") String legacyLanguage
    );
}
