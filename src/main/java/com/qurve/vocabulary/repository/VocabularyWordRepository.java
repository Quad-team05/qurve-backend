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
     * 학습 언어별 유닛 단어 조회
     *
     * * 학습 언어·레벨·유닛에 해당하는 단어를 ID 순으로 조회한다.
     * * 일본어 조회 시 학습 언어가 null인 기존 단어도 포함한다.
     */
    @Query("""
        select w
        from VocabularyWord w
        where w.level = :level
          and w.unitNumber = :unitNumber
          and (
              w.learningLanguage = :language
              or (
                  :language = :legacyLanguage
                  and w.learningLanguage is null
              )
          )
        order by w.wordId asc
        """)
    List<VocabularyWord> findWordsByLanguageAndLevelAndUnitNumber(
            @Param("language") LearningLanguage language,
            @Param("legacyLanguage") LearningLanguage legacyLanguage,
            @Param("level") String level,
            @Param("unitNumber") Integer unitNumber
    );

    /**
     * 학습 언어별 무작위 단어 조회
     *
     * * 선택한 학습 언어의 단어를 지정한 개수까지 무작위로 조회한다.
     * * 일본어 조회 시 학습 언어가 null인 기존 데이터도 포함한다.
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
     * 학습 언어별 유닛 번호 목록 조회
     *
     * * 학습 언어와 레벨에 해당하는 유닛 번호를 오름차순으로 조회한다.
     * * language가 legacyLanguage와 같으면 학습 언어가 null인 기존 데이터도 포함한다.
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

    /**
     * 학습 언어별 북마크 단어 조회
     *
     * * 북마크된 단어 ID 중 지정한 학습 언어의 단어를 ID 오름차순으로 조회한다.
     * * 일본어 조회 시 학습 언어가 null인 기존 단어도 포함한다.
     *
     * @param wordIds 북마크된 단어 ID 목록
     * @param language 조회할 학습 언어
     * @param legacyLanguage 언어가 null인 기존 단어에 적용할 언어
     * @return 학습 언어에 해당하는 북마크 단어 목록
     */
    @Query("""
        select w
        from VocabularyWord w
        where w.wordId in :wordIds
          and (
              w.learningLanguage = :language
              or (
                  :language = :legacyLanguage
                  and w.learningLanguage is null
              )
          )
        order by w.wordId asc
        """)
    List<VocabularyWord> findBookmarkedWordsByLanguage(
            @Param("wordIds") List<Long> wordIds,
            @Param("language") LearningLanguage language,
            @Param("legacyLanguage") LearningLanguage legacyLanguage
    );
}
