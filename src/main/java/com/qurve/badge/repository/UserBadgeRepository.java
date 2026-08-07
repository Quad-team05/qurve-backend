package com.qurve.badge.repository;

import com.qurve.badge.domain.BadgeDefinition;
import com.qurve.badge.domain.UserBadge;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByUserAndBadgeDefinition(User user, BadgeDefinition badgeDefinition);
    List<UserBadge> findAllByUser(User user);
    List<UserBadge> findAllByUserAndBadgeDefinitionIn(User user, Collection<BadgeDefinition> badgeDefinitions);
}
