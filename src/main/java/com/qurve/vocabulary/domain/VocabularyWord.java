package com.qurve.vocabulary.domain;

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
@Table(name = "tb_vocabulary_word")
public class VocabularyWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Long wordId;

    @Column(name = "level", length = 10, nullable = false)
    private String level;

    @Column(name = "unit_number", nullable = false)
    private Integer unitNumber;

    @Column(name = "expression", length = 100, nullable = false)
    private String expression;

    @Column(name = "reading", length = 100, nullable = false)
    private String reading;

    @Column(name = "meaning", length = 500)
    private String meaning;

    @Column(name = "korean_meaning", length = 500)
    private String koreanMeaning;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Column(name = "source", length = 50, nullable = false)
    private String source;

    public void updateKoreanMeaning(String koreanMeaning) {
        this.koreanMeaning = koreanMeaning;
    }
}
