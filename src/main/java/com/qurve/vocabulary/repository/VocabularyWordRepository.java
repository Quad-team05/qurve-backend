package com.qurve.vocabulary.repository;

import com.qurve.vocabulary.domain.VocabularyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    @Query("SELECT v FROM VocabularyWord v ORDER BY FUNCTION('RAND') LIMIT :limit")
    List<VocabularyWord> findRandom(@Param("limit") int limit);
}
