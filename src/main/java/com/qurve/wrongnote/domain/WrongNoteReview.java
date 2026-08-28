package com.qurve.wrongnote.domain;

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
 * 오답노트 학습 종료 단위의 완료 기록입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_wrong_note_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wrong_note_review_user_key", columnNames = {"user_id", "review_key"})
        },
        indexes = {
                @Index(name = "idx_wrong_note_review_user", columnList = "user_id")
        }
)
public class WrongNoteReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_note_review_id")
    private Long wrongNoteReviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "review_key", length = 64, nullable = false)
    private String reviewKey;

    @Column(name = "problem_count", nullable = false)
    private Integer problemCount;
}
