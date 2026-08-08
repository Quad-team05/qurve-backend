package com.qurve.problem.domain;

import com.qurve.global.entity.BaseEntity;
import com.qurve.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_problem_bookmark",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_problem_bookmark_user_problem", columnNames = {"user_id", "problem_id"})
        },
        indexes = {
                @Index(name = "idx_problem_bookmark_user", columnList = "user_id")
        }
)
public class ProblemBookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_bookmark_id")
    private Long problemBookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;
}
