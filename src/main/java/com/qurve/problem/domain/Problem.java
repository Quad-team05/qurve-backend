package com.qurve.problem.domain;

import com.qurve.global.entity.BaseEntity;
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
        name = "tb_problem",
        indexes = {
                @Index(name = "idx_problem_level_category_sub_type", columnList = "level, category, sub_type")
        }
)
public class Problem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    private Long problemId;

    @Column(name = "level", length = 10, nullable = false)
    private String level;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "sub_type", length = 50, nullable = false)
    private String subType;

    @Column(name = "question_format", length = 30, nullable = false)
    private String questionFormat;

    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Lob
    @Column(name = "passage_text")
    private String passageText;

    @Column(name = "answer_index", nullable = false)
    private Integer answerIndex;

    @Lob
    @Column(name = "explanation", nullable = false)
    private String explanation;

    @Column(name = "source_type", length = 30, nullable = false)
    private String sourceType;

    @Column(name = "review_status", length = 30, nullable = false)
    private String reviewStatus;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
