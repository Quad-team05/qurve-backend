package com.qurve.vocabulary.repository;

import com.qurve.user.domain.User;
import com.qurve.vocabulary.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndWordId(User user, Long wordId);
    Optional<Bookmark> findByUserAndWordId(User user, Long wordId);
}