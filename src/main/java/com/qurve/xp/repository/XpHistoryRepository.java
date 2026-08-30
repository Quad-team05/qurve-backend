package com.qurve.xp.repository;

import com.qurve.global.enums.XpActionType;
import com.qurve.user.domain.User;
import com.qurve.xp.domain.XpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface XpHistoryRepository extends JpaRepository<XpHistory, Long> {
    List<XpHistory> findByUserAndEarnedAtBetweenOrderByEarnedAtDesc(User user, LocalDateTime start, LocalDateTime end);
    @Query("""
    SELECT COALESCE(SUM(x.xpAmount), 0)
    FROM XpHistory x
    WHERE x.user = :user
    """)
    Long sumXpAmountByUser(@Param("user") User user);
    List<XpHistory> findByUserOrderByEarnedAtDesc(User user);
    boolean existsByUserAndActionTypeAndEarnedAtBetween(User user, XpActionType actionType, LocalDateTime start, LocalDateTime end);
    boolean existsByUserAndActionTypeAndReferenceId(User user, XpActionType actionType, Long referenceId);
}