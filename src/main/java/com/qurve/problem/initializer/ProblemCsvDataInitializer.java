package com.qurve.problem.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 문제 CSV 데이터 초기화
 *
 * * 서버 시작 시 resources/data/problems 아래의 CSV 파일을 읽어
 * 문제/선택지 테이블에 초기 데이터를 저장한다.
 *
 * * 문제 본문, 음성 스크립트, 선택지가 모두 같은 문제만 중복으로 판단한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemCsvDataInitializer implements ApplicationRunner {

    private static final String DEFAULT_PATH_PATTERN = "classpath*:data/problems/*.csv";
    private static final String SOURCE_TYPE = "CSV";
    private static final String REVIEW_STATUS = "APPROVED";
    private static final String JAPANESE_LANGUAGE = "JA";
    private static final int CHOICE_COUNT = 4;

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    /**
     * 문제 CSV 데이터 저장
     *
     * * 설정이 활성화된 경우에만 CSV 데이터를 적재한다.
     * * 문제 테이블이 없으면 초기화를 건너뛴다.
     *
     * @param args 애플리케이션 실행 인자
     * @throws Exception CSV 파일 처리 실패 시
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (!isSeedEnabled()) {
            return;
        }

        if (!problemTablesExist()) {
            log.warn("Problem seed skipped because tb_problem or tb_problem_choice does not exist.");
            return;
        }

        backfillLegacyProblemLanguage();

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources(getPathPattern());

        if (resources.length == 0) {
            log.info("No problem CSV files found for pattern: {}", getPathPattern());
            return;
        }

        Arrays.sort(resources, Comparator.comparing(resource -> resource.getFilename() == null ? "" : resource.getFilename()));

        int insertedProblemCount = 0;
        int insertedChoiceCount = 0;

        for (Resource resource : resources) {
            ImportResult importResult = importResource(resource);
            insertedProblemCount += importResult.insertedProblemCount();
            insertedChoiceCount += importResult.insertedChoiceCount();
        }

        log.info(
                "Problem seed completed. insertedProblems={}, insertedChoices={}, activeProblems={}",
                insertedProblemCount,
                insertedChoiceCount,
                countActiveProblems()
        );
    }

    private boolean isSeedEnabled() {
        return environment.getProperty("problem.seed.enabled", Boolean.class, false);
    }

    private String getPathPattern() {
        return environment.getProperty("problem.seed.path-pattern", DEFAULT_PATH_PATTERN);
    }

    private boolean problemTablesExist() {
        return tableExists("tb_problem") && tableExists("tb_problem_choice");
    }

    private boolean tableExists(String tableName) {
        try {
            jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
            return true;
        } catch (DataAccessException exception) {
            return false;
        }
    }

    private int countActiveProblems() {
        Integer activeProblemCount = jdbcTemplate.queryForObject(
                "select count(*) from tb_problem where is_active = true",
                Integer.class
        );

        return activeProblemCount == null ? 0 : activeProblemCount;
    }

    /** 기존 JLPT 문제는 언어 컬럼 도입 전 데이터이므로 일본어로 보정한다. */
    private void backfillLegacyProblemLanguage() {
        jdbcTemplate.update(
                """
                update tb_problem
                set language = ?
                where language is null
                  and level like 'N%'
                """,
                JAPANESE_LANGUAGE
        );
    }

    private ImportResult importResource(Resource resource) throws Exception {
        int insertedProblemCount = 0;
        int insertedChoiceCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String headerLine = reader.readLine();

            if (headerLine == null || headerLine.isBlank()) {
                return new ImportResult(0, 0);
            }

            Map<String, Integer> headerIndexMap = createHeaderIndexMap(parseCsvLine(headerLine));
            validateRequiredHeaders(headerIndexMap, resource.getFilename());

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                ProblemSeedRow problemSeedRow = toProblemSeedRow(headerIndexMap, parseCsvLine(line));

                if (!problemSeedRow.isValid()) {
                    continue;
                }

                Long problemId = findProblemId(problemSeedRow);

                if (problemId == null) {
                    problemId = insertProblem(problemSeedRow);
                    insertedProblemCount++;
                }

                if (!choiceExists(problemId)) {
                    insertedChoiceCount += insertChoices(problemId, problemSeedRow.choices());
                }
            }
        }

        log.info("Imported problem CSV file: {} (insertedProblems={}, insertedChoices={})",
                resource.getFilename(),
                insertedProblemCount,
                insertedChoiceCount);

        return new ImportResult(insertedProblemCount, insertedChoiceCount);
    }

    private Map<String, Integer> createHeaderIndexMap(List<String> headers) {
        Map<String, Integer> headerIndexMap = new HashMap<>();

        for (int index = 0; index < headers.size(); index++) {
            headerIndexMap.put(normalizeHeader(headers.get(index)), index);
        }

        return headerIndexMap;
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }

        return header.replace("\uFEFF", "").trim();
    }

    private void validateRequiredHeaders(Map<String, Integer> headerIndexMap, String fileName) {
        List<String> requiredHeaders = headerIndexMap.containsKey("language")
                ? List.of(
                        "language", "cefrLevel", "qurveLevel", "usageType", "category", "subType",
                        "questionFormat", "questionText", "option1", "option2", "option3", "option4",
                        "correctAnswer", "explanation"
                )
                : List.of(
                        "level", "category", "subType", "questionFormat", "questionText",
                        "choice1", "choice2", "choice3", "choice4", "answerIndex", "explanation"
                );

        for (String requiredHeader : requiredHeaders) {
            if (!headerIndexMap.containsKey(requiredHeader)) {
                throw new IllegalArgumentException("Missing required header '" + requiredHeader + "' in " + fileName);
            }
        }
    }

    private ProblemSeedRow toProblemSeedRow(Map<String, Integer> headerIndexMap, List<String> columns) {
        boolean englishFormat = headerIndexMap.containsKey("language");
        List<String> choices = List.of(
                getValue(columns, headerIndexMap, englishFormat ? "option1" : "choice1"),
                getValue(columns, headerIndexMap, englishFormat ? "option2" : "choice2"),
                getValue(columns, headerIndexMap, englishFormat ? "option3" : "choice3"),
                getValue(columns, headerIndexMap, englishFormat ? "option4" : "choice4")
        );

        Integer answerIndex = parseInteger(getValue(
                columns,
                headerIndexMap,
                englishFormat ? "correctAnswer" : "answerIndex"
        ));

        // 기존 문제 API의 선택지 번호는 0~3이므로 영어 CSV의 1~4 정답을 변환한다.
        if (englishFormat && answerIndex != null) {
            answerIndex--;
        }

        return new ProblemSeedRow(
                englishFormat ? getValue(columns, headerIndexMap, "language").toUpperCase(java.util.Locale.ROOT) : JAPANESE_LANGUAGE,
                toNullable(getValue(columns, headerIndexMap, "cefrLevel")),
                englishFormat ? getValue(columns, headerIndexMap, "qurveLevel") : getValue(columns, headerIndexMap, "level"),
                toNullable(getValue(columns, headerIndexMap, "usageType")),
                getValue(columns, headerIndexMap, "category"),
                getValue(columns, headerIndexMap, "subType"),
                getValue(columns, headerIndexMap, "questionFormat"),
                toNullable(getValue(columns, headerIndexMap, "topic")),
                getValue(columns, headerIndexMap, "questionText"),
                toNullable(getValue(columns, headerIndexMap, englishFormat ? "passage" : "passageText")),
                toNullable(getValue(columns, headerIndexMap, "audioScript")),
                answerIndex,
                toNullable(getValue(columns, headerIndexMap, "explanation")),
                toNullable(getValue(columns, headerIndexMap, "koreanTranslation")),
                englishFormat ? getValue(columns, headerIndexMap, "sourceType") : SOURCE_TYPE,
                toNullable(getValue(columns, headerIndexMap, "sourceReference")),
                englishFormat ? getValue(columns, headerIndexMap, "reviewStatus") : REVIEW_STATUS,
                choices
        );
    }

    private String getValue(List<String> columns, Map<String, Integer> headerIndexMap, String headerName) {
        Integer index = headerIndexMap.get(headerName);

        if (index == null || index >= columns.size()) {
            return "";
        }

        return columns.get(index).trim();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Integer.parseInt(value.trim());
    }

    private Long findProblemId(ProblemSeedRow problemSeedRow) {
        List<Long> problemIds = jdbcTemplate.query(
                """
                select problem_id
                from tb_problem
                where language <=> ?
                  and level = ?
                  and category = ?
                  and sub_type = ?
                  and question_format = ?
                  and question_text = ?
                  and passage_text <=> ?
                  and audio_script <=> ?
                order by problem_id asc
                """,
                (resultSet, rowNum) -> resultSet.getLong("problem_id"),
                problemSeedRow.language(),
                problemSeedRow.level(),
                problemSeedRow.category(),
                problemSeedRow.subType(),
                problemSeedRow.questionFormat(),
                problemSeedRow.questionText(),
                problemSeedRow.passageText(),
                problemSeedRow.audioScript()
        );

        return problemIds.stream()
                .filter(problemId -> hasSameChoices(problemId, problemSeedRow.choices()))
                .findFirst()
                .orElse(null);
    }

    private boolean hasSameChoices(Long problemId, List<String> choices) {
        List<String> existingChoices = jdbcTemplate.query(
                """
                select choice_text
                from tb_problem_choice
                where problem_id = ?
                order by choice_number asc
                """,
                (resultSet, rowNum) -> resultSet.getString("choice_text"),
                problemId
        );

        return existingChoices.equals(choices);
    }

    private Long insertProblem(ProblemSeedRow problemSeedRow) {
        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    insert into tb_problem (
                        language,
                        cefr_level,
                        level,
                        usage_type,
                        category,
                        sub_type,
                        question_format,
                        topic,
                        question_text,
                        passage_text,
                        audio_script,
                        answer_index,
                        explanation,
                        korean_translation,
                        source_type,
                        source_reference,
                        review_status,
                        is_active,
                        created_at,
                        updated_at
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            preparedStatement.setString(1, problemSeedRow.language());
            preparedStatement.setString(2, problemSeedRow.cefrLevel());
            preparedStatement.setString(3, problemSeedRow.level());
            preparedStatement.setString(4, problemSeedRow.usageType());
            preparedStatement.setString(5, problemSeedRow.category());
            preparedStatement.setString(6, problemSeedRow.subType());
            preparedStatement.setString(7, problemSeedRow.questionFormat());
            preparedStatement.setString(8, problemSeedRow.topic());
            preparedStatement.setString(9, problemSeedRow.questionText());
            preparedStatement.setString(10, problemSeedRow.passageText());
            preparedStatement.setString(11, problemSeedRow.audioScript());
            preparedStatement.setInt(12, problemSeedRow.answerIndex());
            preparedStatement.setString(13, problemSeedRow.explanation());
            preparedStatement.setString(14, problemSeedRow.koreanTranslation());
            preparedStatement.setString(15, problemSeedRow.sourceType());
            preparedStatement.setString(16, problemSeedRow.sourceReference());
            preparedStatement.setString(17, problemSeedRow.reviewStatus());
            preparedStatement.setBoolean(18, true);
            preparedStatement.setTimestamp(19, Timestamp.valueOf(now));
            preparedStatement.setTimestamp(20, Timestamp.valueOf(now));
            return preparedStatement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("Failed to insert problem: generated key is null.");
        }

        return key.longValue();
    }

    private boolean choiceExists(Long problemId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from tb_problem_choice where problem_id = ?",
                Integer.class,
                problemId
        );

        return count != null && count >= CHOICE_COUNT;
    }

    private int insertChoices(Long problemId, List<String> choices) {
        int insertedChoiceCount = 0;

        for (int index = 0; index < choices.size(); index++) {
            if (choiceNumberExists(problemId, index)) {
                continue;
            }

            String choiceText = toNullable(choices.get(index));

            if (choiceText == null) {
                continue;
            }

            jdbcTemplate.update(
                    """
                    insert into tb_problem_choice (
                        problem_id,
                        choice_number,
                        choice_text
                    ) values (?, ?, ?)
                    """,
                    problemId,
                    index,
                    choiceText
            );
            insertedChoiceCount++;
        }

        return insertedChoiceCount;
    }

    private boolean choiceNumberExists(Long problemId, int choiceNumber) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from tb_problem_choice
                where problem_id = ?
                  and choice_number = ?
                """,
                Integer.class,
                problemId,
                choiceNumber
        );

        return count != null && count > 0;
    }

    /**
     * CSV 한 줄 파싱
     *
     * * 설명이나 지문에 쉼표가 포함될 수 있으므로
     * 따옴표를 고려해 컬럼을 분리한다.
     *
     * @param line CSV 한 줄
     * @return 분리된 컬럼 목록
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char currentCharacter = line.charAt(index);

            if (currentCharacter == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentCharacter == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentCharacter);
            }
        }

        result.add(current.toString());
        return result;
    }

    private String toNullable(String value) {
        String trimmedValue = value == null ? "" : value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private record ImportResult(int insertedProblemCount, int insertedChoiceCount) {
    }

    private record ProblemSeedRow(
            String language,
            String cefrLevel,
            String level,
            String usageType,
            String category,
            String subType,
            String questionFormat,
            String topic,
            String questionText,
            String passageText,
            String audioScript,
            Integer answerIndex,
            String explanation,
            String koreanTranslation,
            String sourceType,
            String sourceReference,
            String reviewStatus,
            List<String> choices
    ) {
        private boolean isValid() {
            return StreamSupport.allRequiredFieldsFilled(level, category, subType, questionFormat, questionText, explanation)
                    && answerIndex != null
                    && answerIndex >= 0
                    && answerIndex < CHOICE_COUNT
                    && choices.stream().filter(Objects::nonNull).map(String::trim).noneMatch(String::isBlank);
        }
    }

    private static final class StreamSupport {

        private StreamSupport() {
        }

        private static boolean allRequiredFieldsFilled(String... values) {
            for (String value : values) {
                if (value == null || value.isBlank()) {
                    return false;
                }
            }

            return true;
        }
    }
}
