package com.qurve.expression.domain;

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
@Table(name = "tb_basic_expression")
public class BasicExpression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_id")
    private Long expressionId;

    @Column(name = "order_number", nullable = false, unique = true)
    private Integer orderNumber;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "kanji", length = 500, nullable = false)
    private String kanji;

    @Column(name = "hiragana", length = 500, nullable = false)
    private String hiragana;

    @Column(name = "romaji", length = 500, nullable = false)
    private String romaji;

    @Column(name = "translation", length = 500, nullable = false)
    private String translation;
}