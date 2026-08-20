package com.qurve.learning.service;

import com.qurve.attendance.domain.StudyStatistics;
import com.qurve.attendance.repository.StudyStatisticsRepository;
import com.qurve.badge.service.BadgeService;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.learning.domain.StudyTimeRecord;
import com.qurve.learning.dto.request.StudyTimeSaveRequestDto;
import com.qurve.learning.dto.response.DailyStudyTimeResponseDto;
import com.qurve.learning.dto.response.StudyTimeSaveResponseDto;
import com.qurve.learning.dto.response.StudyTimeStatisticsResponseDto;
import com.qurve.learning.dto.response.TodayLearningResponseDto;
import com.qurve.learning.repository.StudyTimeRecordRepository;
import com.qurve.problem.repository.ProblemRepository;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningService {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final List<String> DAY_OF_WEEK_LABELS = List.of("월", "화", "수", "목", "금", "토", "일");
    private static final List<String> JLPT_LEVELS = List.of("N1", "N2", "N3", "N4", "N5");
    private static final int DEFAULT_TODAY_LEARNING_SET_SIZE = 20;

    private final UserRepository userRepository;
    private final StudyTimeRecordRepository studyTimeRecordRepository;
    private final StudyStatisticsRepository studyStatisticsRepository;
    private final ProblemRepository problemRepository;
    private final BadgeService badgeService;

    /**
     * 오늘의 학습 카드 조회
     *
     * * KST 기준 오늘 날짜와 사용자의 현재 레벨을 기준으로
     * 오늘의 학습 세트를 선택해 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 오늘의 학습 카드 응답 정보
     * @throws BusinessException 유저가 존재하지 않거나 오늘의 학습 세트가 없는 경우
     */
    public TodayLearningResponseDto findTodayLearning(String loginId) {
        User user = findUserByLoginId(loginId);
        LocalDate today = LocalDate.now(KST_ZONE);
        String preferredLevel = mapCurrentLevelToJlptLevel(user.getCurrentLevel());

        TodayLearningSet todayLearningSet = findTodayLearningSet(preferredLevel, today);

        return TodayLearningResponseDto.of(
                todayLearningSet.level(),
                todayLearningSet.categoryCode(),
                todayLearningSet.subTypeCode(),
                todayLearningSet.categoryLabel(),
                todayLearningSet.titleLabel(),
                todayLearningSet.totalQuestionCount(),
                todayLearningSet.estimatedMinutes()
        );
    }

    /**
     * 학습 시간 통계 조회
     *
     * * KST 기준 이번 주 월요일부터 일요일까지의 요일별 학습 시간,
     * 오늘 학습 시간, 이번 주 전체 학습 시간을 분 단위로 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 학습 시간 통계 응답 정보
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public StudyTimeStatisticsResponseDto findStudyTimeStatistics(String loginId) {
        User user = findUserByLoginId(loginId);
        LocalDate today = LocalDate.now(KST_ZONE);
        LocalDate weekStartDate = today.with(DayOfWeek.MONDAY);
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        Map<LocalDate, Integer> studyTimeByDate = studyTimeRecordRepository
                .findAllByUserAndStudyDateBetween(user, weekStartDate, weekEndDate)
                .stream()
                .collect(Collectors.toMap(
                        StudyTimeRecord::getStudyDate,
                        StudyTimeRecord::getStudyTimeMinutes,
                        Integer::sum
                ));

        List<DailyStudyTimeResponseDto> dailyStudyTimes = IntStream.range(0, 7)
                .mapToObj(index -> {
                    LocalDate targetDate = weekStartDate.plusDays(index);
                    DayOfWeek dayOfWeek = targetDate.getDayOfWeek();
                    int studyTimeMinutes = studyTimeByDate.getOrDefault(targetDate, 0);

                    return DailyStudyTimeResponseDto.of(
                            dayOfWeek.name(),
                            DAY_OF_WEEK_LABELS.get(dayOfWeek.getValue() - 1),
                            studyTimeMinutes
                    );
                })
                .toList();

        int todayStudyTimeMinutes = studyTimeByDate.getOrDefault(today, 0);
        int weeklyStudyTimeMinutes = studyTimeByDate.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        return StudyTimeStatisticsResponseDto.of(
                weekStartDate,
                weekEndDate,
                todayStudyTimeMinutes,
                weeklyStudyTimeMinutes,
                dailyStudyTimes
        );
    }

    /**
     * 학습 시간 저장
     *
     * * 오늘 날짜 기준 학습 시간 레코드를 생성하거나 누적하고,
     * 전체 누적 학습 시간도 함께 갱신한다.
     *
     * @param loginId 로그인 ID
     * @param requestDto 추가할 학습 시간
     * @return 추가된 학습 시간과 누적 학습 시간
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    @Transactional
    public StudyTimeSaveResponseDto saveStudyTime(String loginId, StudyTimeSaveRequestDto requestDto) {
        User user = findUserByLoginId(loginId);
        StudyStatistics studyStatistics = findOrCreateStudyStatistics(user);
        StudyTimeRecord studyTimeRecord = findOrCreateStudyTimeRecord(user, LocalDate.now(KST_ZONE));

        int studyTimeMinutes = requestDto.getStudyTimeMinutes();
        studyTimeRecord.addStudyTime(studyTimeMinutes);
        studyStatistics.addStudyTime(studyTimeMinutes);
        badgeService.evaluate(user);

        return StudyTimeSaveResponseDto.of(studyTimeMinutes, studyStatistics);
    }

    private User findUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private StudyStatistics findOrCreateStudyStatistics(User user) {
        return studyStatisticsRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> studyStatisticsRepository.save(StudyStatistics.create(user)));
    }

    private StudyTimeRecord findOrCreateStudyTimeRecord(User user, LocalDate studyDate) {
        return studyTimeRecordRepository.findByUserAndStudyDate(user, studyDate)
                .orElseGet(() -> studyTimeRecordRepository.save(StudyTimeRecord.create(user, studyDate)));
    }

    private TodayLearningSet findTodayLearningSet(String preferredLevel, LocalDate today) {
        for (String candidateLevel : createLevelFallbackOrder(preferredLevel)) {
            List<ProblemRepository.TodayLearningSetProjection> learningSets = problemRepository
                    .findTodayLearningSetsByLevel(candidateLevel);

            if (learningSets.isEmpty()) {
                continue;
            }

            int index = Math.floorMod(today.toEpochDay(), learningSets.size());
            ProblemRepository.TodayLearningSetProjection learningSet = learningSets.get(index);
            int totalQuestionCount = Math.min(DEFAULT_TODAY_LEARNING_SET_SIZE, learningSet.getProblemCount().intValue());

            return new TodayLearningSet(
                    candidateLevel,
                    learningSet.getCategory(),
                    learningSet.getSubType(),
                    toCategoryLabel(learningSet.getCategory(), learningSet.getSubType()),
                    toTitleLabel(learningSet.getSubType()),
                    totalQuestionCount,
                    Math.max(1, (int) Math.ceil(totalQuestionCount / 2.0))
            );
        }

        throw new BusinessException(ErrorCode.TODAY_LEARNING_NOT_FOUND);
    }

    private String mapCurrentLevelToJlptLevel(Integer currentLevel) {
        if (currentLevel == null) {
            return "N5";
        }

        return switch (currentLevel) {
            case 1, 2 -> "N5";
            case 3, 4 -> "N4";
            case 5, 6 -> "N3";
            case 7, 8 -> "N2";
            case 9, 10 -> "N1";
            default -> "N5";
        };
    }

    private List<String> createLevelFallbackOrder(String preferredLevel) {
        int preferredIndex = JLPT_LEVELS.indexOf(preferredLevel);

        if (preferredIndex < 0) {
            return List.of("N5");
        }

        List<String> fallbackOrder = new ArrayList<>();

        for (int index = preferredIndex; index < JLPT_LEVELS.size(); index++) {
            fallbackOrder.add(JLPT_LEVELS.get(index));
        }

        for (int index = preferredIndex - 1; index >= 0; index--) {
            fallbackOrder.add(JLPT_LEVELS.get(index));
        }

        return fallbackOrder;
    }

    private String toCategoryLabel(String categoryCode, String subTypeCode) {
        String normalizedCategoryCode = normalizeKeyword(categoryCode);
        String normalizedSubTypeCode = normalizeKeyword(subTypeCode);

        return switch (normalizedCategoryCode) {
            case "READING" -> "독해";
            case "GRAMMAR" -> "문법";
            case "LANGUAGE_KNOWLEDGE" -> switch (normalizedSubTypeCode) {
                case "GRAMMAR_PATTERN" -> "문법";
                default -> "문자/어휘";
            };
            default -> normalizedCategoryCode;
        };
    }

    private String toTitleLabel(String subTypeCode) {
        String normalizedSubTypeCode = normalizeKeyword(subTypeCode);

        return switch (normalizedSubTypeCode) {
            case "KANJI_READING" -> "한자 읽기";
            case "CONTEXT_VOCABULARY" -> "문맥 규정";
            case "USAGE" -> "용법";
            case "GRAMMAR_PATTERN" -> "문법";
            case "READING_COMPREHENSION" -> "독해";
            default -> normalizedSubTypeCode;
        };
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toUpperCase(Locale.ROOT);
    }

    private record TodayLearningSet(
            String level,
            String categoryCode,
            String subTypeCode,
            String categoryLabel,
            String titleLabel,
            int totalQuestionCount,
            int estimatedMinutes
    ) {
    }
}
