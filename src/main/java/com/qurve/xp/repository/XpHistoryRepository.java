package com.qurve.xp.repository;

import com.qurve.user.domain.User;
import com.qurve.xp.domain.XpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface XpHistoryRepository extends JpaRepository<XpHistory, Long> {
    @Query("""
    SELECT COALESCE(SUM(x.xpAmount), 0)
    FROM XpHistory x
    WHERE x.user = :user
    """)
    Long sumXpAmountByUser(@Param("user") User user);
}
