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
        name = "tb_problem_submission",
        indexes = {
                @Index(name = "idx_problem_submission_user_problem", columnList = "user_id, problem_id")
        }
)
public class ProblemSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "selected_choice_number", nullable = false)
    private Integer selectedChoiceNumber;

    @Column(name = "answer_choice_number", nullable = false)
    private Integer answerChoiceNumber;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;
}
