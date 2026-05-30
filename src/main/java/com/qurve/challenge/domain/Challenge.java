package com.qurve.challenge.domain;

import com.qurve.global.entity.BaseEntity;
import com.qurve.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_challenge")
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long challengeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private ChallengeGoalType goalType;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(name = "current_value", nullable = false)
    private Integer currentValue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChallengeStatus status;
}