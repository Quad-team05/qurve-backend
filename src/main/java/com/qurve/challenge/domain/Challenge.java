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
    @Column(name = "goal_type", length = 50, nullable = false)
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

    /**
     * 챌린지 진행도를 누적하고 목표를 달성하면 완료 상태로 변경합니다.
     */
    public void addProgress(int amount) {
        if (amount <= 0 || status != ChallengeStatus.ACTIVE) {
            return;
        }

        this.currentValue = Math.min(this.currentValue + amount, this.targetValue);

        if (this.currentValue >= this.targetValue) {
            this.status = ChallengeStatus.COMPLETED;
        }
    }

    /**
     * 활동 발생일이 챌린지 기간에 포함되는지 확인합니다.
     */
    public boolean isActiveOn(LocalDate date) {
        return status == ChallengeStatus.ACTIVE
                && !date.isBefore(startDate)
                && !date.isAfter(endDate);
    }

    /**
     * 진행 중인 챌린지의 기본 정보를 수정합니다.
     *
     * * 목표 유형은 기존 학습 이력의 기준이므로 변경하지 않습니다.
     */
    public void update(String title, int targetValue, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.targetValue = targetValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentValue = Math.min(currentValue, targetValue);

        if (currentValue >= targetValue) {
            status = ChallengeStatus.COMPLETED;
        }
    }
}
