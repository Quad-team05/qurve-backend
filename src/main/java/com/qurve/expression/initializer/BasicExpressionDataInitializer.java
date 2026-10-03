package com.qurve.expression.initializer;

import com.qurve.expression.domain.BasicExpression;
import com.qurve.expression.repository.BasicExpressionRepository;
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

@Component
@RequiredArgsConstructor
public class BasicExpressionDataInitializer implements ApplicationRunner {

    private static final String SEED_FILE_PATH = "data/basic_travel_japanese_100.csv";

    private static final List<String> EXPECTED_HEADER = List.of(
            "category",
            "kanji",
            "hiragana",
            "romaji",
            "translation"
    );

    private final BasicExpressionRepository basicExpressionRepository;

    /**
     * 초급 표현 CSV 데이터 저장
     *
     * * 서버 시작 시 CSV를 읽어 초급 표현 테이블에 저장한다.
     * 이미 데이터가 있으면 중복 저장하지 않는다.
     *
     * @param args 애플리케이션 실행 인자
     * @throws Exception CSV 처리 또는 DB 저장에 실패한 경우
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        if (basicExpressionRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(SEED_FILE_PATH);

        if (!resource.exists()) {
            throw new IllegalStateException("초급 표현 CSV 파일을 찾을 수 없습니다: " + SEED_FILE_PATH);
        }

        List<BasicExpression> expressions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();

            if (header == null) {
                throw new IllegalStateException("초급 표현 CSV 파일이 비어 있습니다.");
            }

            // 첨부 CSV의 UTF-8 BOM을 제거한 뒤 헤더를 확인한다.
            List<String> headerColumns = parseCsvLine(header.replace("\uFEFF", ""))
                    .stream()
                    .map(String::trim)
                    .toList();

            if (!EXPECTED_HEADER.equals(headerColumns)) {
                throw new IllegalStateException("초급 표현 CSV 헤더가 올바르지 않습니다.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                List<String> columns = parseCsvLine(line)
                        .stream()
                        .map(String::trim)
                        .toList();

                if (columns.size() != EXPECTED_HEADER.size() || columns.stream().anyMatch(String::isBlank)) {
                    throw new IllegalStateException("초급 표현 CSV 데이터가 올바르지 않습니다. 행: " + lineNumber);
                }

                expressions.add(BasicExpression.builder()
                        .orderNumber(expressions.size() + 1)
                        .category(columns.get(0))
                        .kanji(columns.get(1))
                        .hiragana(columns.get(2))
                        .romaji(columns.get(3))
                        .translation(columns.get(4))
                        .build());
            }
        }

        if (expressions.isEmpty()) {
            throw new IllegalStateException("초급 표현 CSV에 저장할 표현이 없습니다.");
        }

        basicExpressionRepository.saveAll(expressions);
    }

    /**
     * CSV 한 줄 파싱
     *
     * * 따옴표 안의 쉼표와 이중 따옴표를 고려해 컬럼을 분리한다.
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
            throw new IllegalStateException("CSV 따옴표가 닫히지 않았습니다.");
        }

        result.add(current.toString());
        return result;
    }
}