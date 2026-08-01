package com.qurve.problem.domain;

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
        name = "tb_problem_choice",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_problem_choice_number", columnNames = {"problem_id", "choice_number"})
        }
)
public class ProblemChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_id")
    private Long choiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "choice_number", nullable = false)
    private Integer choiceNumber;

    @Column(name = "choice_text", length = 500, nullable = false)
    private String choiceText;
}
