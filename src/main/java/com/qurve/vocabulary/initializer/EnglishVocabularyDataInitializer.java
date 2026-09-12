package com.qurve.vocabulary.initializer;

import com.qurve.global.enums.LearningLanguage;
import com.qurve.vocabulary.domain.VocabularyWord;
import com.qurve.vocabulary.repository.VocabularyWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 영어 단어 데이터 초기화
 *
 * * 영어 단어 CSV를 읽어 단어 마스터 데이터를 초기화한다.
 *
 * * 출처와 원본 항목 ID를 기준으로 기존 단어를 식별한다.
 * * 없는 단어는 추가하고, 기존 단어는 한국어 뜻만 갱신한다.
 * * 레벨과 유닛 번호는 CSV에 지정된 값을 사용한다.
 */
@Component
@RequiredArgsConstructor
public class EnglishVocabularyDataInitializer implements ApplicationRunner {

    private static final String FILE_PATH = "data/english_vocabulary.csv";

    private static final List<String> EXPECTED_HEADER = List.of(
            "entryId",
            "cefrLevel",
            "unitNumber",
            "expression",
            "partOfSpeech",
            "koreanMeaning",
            "source"
    );

    private static final Set<String> SUPPORTED_LEVELS =
            Set.of("A1", "A2", "B1", "B2", "C1", "C2");

    private final VocabularyWordRepository vocabularyWordRepository;

    /**
     * 영어 단어 CSV 데이터 저장
     *
     * * 애플리케이션 시작 시 영어 단어 데이터를 저장한다.
     *
     * * CSV의 헤더, 필수 값과 식별자 중복을 검증한다.
     * * 기존 단어의 한국어 뜻 변경은 트랜잭션의 변경 감지로 반영하고,
     * * 신규 단어는 일괄 저장한다.
     *
     * @param args 애플리케이션 실행 인자
     * @throws Exception CSV 읽기, 데이터 검증 또는 저장에 실패한 경우
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        ClassPathResource resource = new ClassPathResource(FILE_PATH);

        // 출처별 기존 데이터를 한 번씩 조회해 중복 저장을 방지한다.
        Map<String, Map<String, VocabularyWord>> existingBySource = new HashMap<>();

        // DB 중복 여부와 별개로, CSV 내부의 출처·항목 ID 중복을 검증한다.
        Set<SeedKey> seenKeys = new HashSet<>();
        List<VocabularyWord> newWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
        ) {
            String header = reader.readLine();

            if (header == null || !parseCsvLine(header.replace("\uFEFF", "")).equals(EXPECTED_HEADER)) {
                throw new IllegalStateException("영어 단어 CSV 헤더가 올바르지 않습니다.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                List<String> columns = parseCsvLine(line);

                if (columns.size() != EXPECTED_HEADER.size()) {
                    throw new IllegalStateException("영어 단어 CSV 컬럼 수 오류: " + lineNumber + "행");
                }

                String entryId = columns.get(0).trim();
                String level = columns.get(1).trim();
                int unitNumber = Integer.parseInt(columns.get(2).trim());
                String expression = columns.get(3).trim();
                String partOfSpeech = columns.get(4).trim();
                String koreanMeaning = columns.get(5).trim();
                String source = columns.get(6).trim();

                if (entryId.isBlank()
                        || !SUPPORTED_LEVELS.contains(level)
                        || unitNumber < 1
                        || expression.isBlank()
                        || koreanMeaning.isBlank()
                        || source.isBlank()) {
                    throw new IllegalStateException("영어 단어 CSV 필수 값 오류: " + lineNumber + "행");
                }

                SeedKey key = new SeedKey(source, entryId);

                if (!seenKeys.add(key)) {
                    throw new IllegalStateException("영어 단어 CSV 식별자 중복: " + entryId);
                }

                Map<String, VocabularyWord> existingWords = existingBySource.computeIfAbsent(source, this::loadExistingWords);

                VocabularyWord existingWord = existingWords.get(entryId);

                if (existingWord != null) {
                    // 재시작 시 CSV의 최신 한국어 뜻을 반영한다.
                    existingWord.updateKoreanMeaning(koreanMeaning);
                    continue;
                }

                newWords.add(VocabularyWord.builder()
                        .learningLanguage(LearningLanguage.ENGLISH)
                        .sourceEntryId(entryId)
                        .level(level)
                        .unitNumber(unitNumber)
                        .expression(expression)
                        .partOfSpeech(partOfSpeech.isBlank() ? null : partOfSpeech)
                        .koreanMeaning(koreanMeaning)
                        .reading(null)
                        .meaning(null)
                        .source(source)
                        .build());
            }
        }

        vocabularyWordRepository.saveAll(newWords);
    }

    /**
     * 출처별 기존 단어 조회
     *
     * * 지정한 출처의 기존 단어를 원본 항목 ID 기준으로 조회한다.
     * * 원본 항목 ID가 없는 데이터는 매핑 대상에서 제외한다.
     */
    private Map<String, VocabularyWord> loadExistingWords(String source) {
        return vocabularyWordRepository.findBySource(source)
                .stream()
                .filter(word -> word.getSourceEntryId() != null)
                .collect(Collectors.toMap(
                        VocabularyWord::getSourceEntryId,
                        word -> word
                ));
    }

    /**
     * CSV 한 줄 파싱
     *
     * * 따옴표 안의 쉼표와 이스케이프된 따옴표를 처리한다.
     * * 현재 CSV처럼 각 항목이 한 줄에 저장된 형식을 대상으로 한다.
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        if (inQuotes) {
            throw new IllegalStateException("영어 단어 CSV의 따옴표가 닫히지 않았습니다.");
        }

        result.add(current.toString());
        return result;
    }

    private record SeedKey(String source, String entryId) {
    }
}