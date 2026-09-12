package com.qurve.vocabulary.repository;

import com.qurve.global.enums.LearningLanguage;
import com.qurve.vocabulary.domain.VocabularyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VocabularyWordRepository extends JpaRepository<VocabularyWord, Long> {

    long countBySource(String source);

    List<VocabularyWord> findBySource(String source);

    @Query("""
            select distinct w.unitNumber
            from VocabularyWord w
            where w.level = :level
            order by w.unitNumber asc
            """)
    List<Integer> findDistinctUnitNumbersByLevel(@Param("level") String level);

    List<VocabularyWord> findByLevelAndUnitNumberOrderByWordIdAsc(String level, Integer unitNumber);

    /**
     * 선택한 학습 언어의 단어를 지정한 개수까지 무작위로 조회한다.
     * 일본어 조회 시 학습 언어가 null인 기존 데이터도 포함한다.
     */
    @Query("""
        select v
        from VocabularyWord v
        where v.learningLanguage = :language
           or (
               :language = :legacyLanguage
               and v.learningLanguage is null
           )
        order by function('RAND')
        limit :limit
        """)
    List<VocabularyWord> findRandomByLanguage(
            @Param("language") LearningLanguage language,
            @Param("legacyLanguage") LearningLanguage legacyLanguage,
            @Param("limit") int limit
    );

    /**
     * 학습 언어와 레벨에 해당하는 유닛 번호를 오름차순으로 조회한다.
     * language가 legacyLanguage와 같으면 학습 언어가 null인 기존 데이터도 포함한다.
     *
     * @param legacyLanguage 언어가 null인 기존 데이터에 적용할 언어
     */
    @Query("""
        select distinct w.unitNumber
        from VocabularyWord w
        where w.level = :level
          and (
              w.learningLanguage = :language
              or (
                  :language = :legacyLanguage
                  and w.learningLanguage is null
              )
          )
        order by w.unitNumber asc
        """)
    List<Integer> findUnitNumbersByLanguageAndLevel(
            @Param("language") LearningLanguage language,
            @Param("legacyLanguage") LearningLanguage legacyLanguage,
            @Param("level") String level
    );

    Optional<VocabularyWord> findBySourceAndSourceEntryId(String source, String sourceEntryId);
}
