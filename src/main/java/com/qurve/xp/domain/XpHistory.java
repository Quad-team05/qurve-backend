package com.qurve.xp.domain;

import com.qurve.global.enums.XpActionType;
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
@Table(name = "tb_xp_history")
public class XpHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "xp_history_id")
    private Long xpHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private XpActionType actionType;

    @Column(name = "xp_amount", nullable = false)
    private Integer xpAmount;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;
}
