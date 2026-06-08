package com.qurve.vocabulary.repository;

import com.qurve.vocabulary.domain.UnitProgress;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitProgressRepository extends JpaRepository<UnitProgress, Long> {
    List<UnitProgress> findByUserAndLevel(User user, String level);
    Optional<UnitProgress> findByUserAndLevelAndUnitNumber(User user, String level, Integer unitNumber);
}