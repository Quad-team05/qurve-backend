package com.qurve.badge.domain;

import com.qurve.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_user_badge",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_badge_user_code", columnNames = {"user_id", "badge_code"})
        },
        indexes = {
                @Index(name = "idx_user_badge_user", columnList = "user_id")
        }
)
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_badge_id")
    private Long userBadgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_code", length = 80, nullable = false)
    private BadgeDefinition badgeDefinition;

    @Column(name = "achieved_at", nullable = false)
    private LocalDateTime achievedAt;
}
