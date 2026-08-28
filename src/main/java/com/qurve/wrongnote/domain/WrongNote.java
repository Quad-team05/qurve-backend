package com.qurve.wrongnote.domain;

import com.qurve.global.entity.BaseEntity;
import com.qurve.problem.domain.Problem;
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

import java.time.LocalDateTime;

/**
 * 오답 문제와 복습 상태를 관리합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_wrong_note",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wrong_note_user_problem", columnNames = {"user_id", "problem_id"})
        },
        indexes = {
                @Index(name = "idx_wrong_note_user", columnList = "user_id")
        }
)
public class WrongNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_note_id")
    private Long wrongNoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Builder.Default
    @Column(name = "reviewed", nullable = false)
    private boolean reviewed = false;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Builder.Default
    @Column(name = "retry_correct", nullable = false)
    private boolean retryCorrect = false;

    @Column(name = "retry_correct_at")
    private LocalDateTime retryCorrectAt;

    public void completeReview(LocalDateTime reviewedAt) {
        this.reviewed = true;
        this.reviewedAt = reviewedAt;
    }

    public void markRetryCorrect(LocalDateTime retryCorrectAt) {
        if (reviewed && !retryCorrect) {
            this.retryCorrect = true;
            this.retryCorrectAt = retryCorrectAt;
        }
    }
}
