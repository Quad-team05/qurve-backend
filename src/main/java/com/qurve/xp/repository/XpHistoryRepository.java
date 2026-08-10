package com.qurve.xp.repository;

import com.qurve.user.domain.User;
import com.qurve.xp.domain.XpHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface XpHistoryRepository extends JpaRepository<XpHistory, Long> {
    List<XpHistory> findByUserAndEarnedAtBetweenOrderByEarnedAtDesc(User user, LocalDateTime start, LocalDateTime end);
    List<XpHistory> findByUserOrderByEarnedAtDesc(User user);
}
