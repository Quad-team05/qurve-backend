package com.qurve.vocabulary.repository;

import com.qurve.global.enums.LearningLanguage;
import com.qurve.user.domain.User;
import com.qurve.vocabulary.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndWordId(User user, Long wordId);
    Optional<Bookmark> findByUserAndWordId(User user, Long wordId);
    List<Bookmark> findByUser(User user);
    long countByUser(User user);

    @Query("""
            select b
            from Bookmark b
            where b.user = :user
              and (b.learningLanguage = :learningLanguage
                   or (:learningLanguage = :legacyLanguage and b.learningLanguage is null))
            order by b.createdAt desc
            """)
    List<Bookmark> findAllByUserAndLearningLanguage(
            @Param("user") User user,
            @Param("learningLanguage") LearningLanguage learningLanguage,
            @Param("legacyLanguage") LearningLanguage legacyLanguage
    );

    @Query("""
            select count(b)
            from Bookmark b
            where b.user = :user
              and (b.learningLanguage = :learningLanguage
                   or (:learningLanguage = :legacyLanguage and b.learningLanguage is null))
            """)
    long countByUserAndLearningLanguage(
            @Param("user") User user,
            @Param("learningLanguage") LearningLanguage learningLanguage,
            @Param("legacyLanguage") LearningLanguage legacyLanguage
    );
}
