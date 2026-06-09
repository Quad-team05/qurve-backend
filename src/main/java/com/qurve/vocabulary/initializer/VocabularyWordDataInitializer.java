package com.qurve.vocabulary.initializer;

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
import java.util.List;
import java.util.Map;

/**
 * JLPT 단어 데이터 초기화
 *
 * * 외부 단어 데이터를 DB에 미리 저장해
 * 단어장 API가 내부 데이터 기준으로 동작하도록 한다.
 */
@Component
@RequiredArgsConstructor
public class VocabularyWordDataInitializer implements ApplicationRunner {

    private static final String SOURCE = "ELZUP";
    private static final Map<String, String> SEED_FILE_PATHS = Map.of(
            "N1", "data/elzup_jlpt_n1.csv",
            "N2", "data/elzup_jlpt_n2.csv",
            "N3", "data/elzup_jlpt_n3.csv",
            "N4", "data/elzup_jlpt_n4.csv",
            "N5", "data/elzup_jlpt_n5.csv"
    );
    // elzup 데이터에는 유닛 구분이 없으므로 20개 단어 단위로 유닛을 나눔
    private static final int WORDS_PER_UNIT = 20;

    private final VocabularyWordRepository vocabularyWordRepository;

    /**
     * JLPT 단어 CSV 데이터 저장
     *
     * * 서버 시작 시 CSV 파일을 읽어 단어 마스터 테이블에 저장한다.
     *
     * * 이미 저장된 ELZUP 데이터가 있으면 중복 저장을 방지하기 위해 추가 저장하지 않는다.
     *
     * @param args 애플리케이션 실행 인자
     * @throws Exception CSV 파일 처리 또는 DB 저장에 실패한 경우
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        // 서버 재시작 시 동일한 단어가 중복 저장되지 않도록 기존 데이터 여부 확인
        if (vocabularyWordRepository.countBySource(SOURCE) > 0) {
            return;
        }

        List<VocabularyWord> words = new ArrayList<>();

        for (Map.Entry<String, String> entry : SEED_FILE_PATHS.entrySet()) {
            String level = entry.getKey();
            ClassPathResource resource = new ClassPathResource(entry.getValue());

            if (!resource.exists()) {
                continue;
            }

            int levelWordCount = 0;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                boolean isHeader = true;

                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    if (line.isBlank()) {
                        continue;
                    }

                    List<String> columns = parseCsvLine(line);

                    if (columns.size() < 3) {
                        continue;
                    }

                    String expression = columns.get(0).trim();
                    String reading = columns.get(1).trim();
                    String meaning = toNullable(columns.get(2));

                    if (expression.isBlank() || reading.isBlank()) {
                        continue;
                    }

                    // 같은 레벨 안에서 20개 단어마다 다음 유닛 번호를 부여한다.
                    int unitNumber = (levelWordCount / WORDS_PER_UNIT) + 1;
                    levelWordCount++;

                    words.add(VocabularyWord.builder()
                            .level(level)
                            .unitNumber(unitNumber)
                            .expression(expression)
                            .reading(reading)
                            .meaning(meaning)
                            .partOfSpeech(null)
                            .source(SOURCE)
                            .build());
                }
            }

        }

        vocabularyWordRepository.saveAll(words);

    }

    /**
     * CSV 한 줄 파싱
     *
     * * 단어 뜻에 쉼표가 포함될 수 있으므로
     * 단순 split 대신 따옴표를 고려해 컬럼을 분리한다.
     *
     * @param line CSV 한 줄
     * @return 분리된 컬럼 목록
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
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

        result.add(current.toString());
        return result;
    }

    private String toNullable(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}