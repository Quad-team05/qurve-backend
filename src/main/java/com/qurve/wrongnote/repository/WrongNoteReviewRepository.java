package com.qurve.wrongnote.repository;

import com.qurve.user.domain.User;
import com.qurve.wrongnote.domain.WrongNoteReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WrongNoteReviewRepository extends JpaRepository<WrongNoteReview, Long> {
    Optional<WrongNoteReview> findByUserAndReviewKey(User user, String reviewKey);
}
