package com.qurve.vocabulary.repository;

import com.qurve.user.domain.User;
import com.qurve.vocabulary.domain.UserWordStudy;
import com.qurve.vocabulary.domain.VocabularyWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserWordStudyRepository extends JpaRepository<UserWordStudy, Long> {
    List<UserWordStudy> findAllByUserAndWordIn(User user, Collection<VocabularyWord> words);
}
