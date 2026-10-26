package com.qurve.problem.domain;

import com.qurve.global.entity.BaseEntity;
import com.qurve.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 완료한 문제 세트 기록입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_problem_set_completion",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_problem_set_completion_user_key", columnNames = {"user_id", "set_key"})
        },
        indexes = {
                @Index(name = "idx_problem_set_completion_user", columnList = "user_id")
        }
)
public class ProblemSetCompletion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_set_completion_id")
    private Long problemSetCompletionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "set_key", length = 64, nullable = false)
    private String setKey;

    @Column(name = "problem_count", nullable = false)
    private Integer problemCount;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "is_perfect", nullable = false)
    private boolean perfect;
}
