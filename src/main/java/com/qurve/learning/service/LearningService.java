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
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningService {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final List<String> DAY_OF_WEEK_LABELS = List.of("월", "화", "수", "목", "금", "토", "일");
    private static final String TODAY_LEARNING_CATEGORY = "문자/어휘";
    private static final String TODAY_LEARNING_TITLE = "문맥규정";
    private static final int TODAY_LEARNING_TOTAL_QUESTION_COUNT = 20;
    private static final int TODAY_LEARNING_ESTIMATED_MINUTES = 10;

    private final UserRepository userRepository;
    private final StudyTimeRecordRepository studyTimeRecordRepository;
    private final StudyStatisticsRepository studyStatisticsRepository;
    private final BadgeService badgeService;

    /**
     * 오늘의 학습 카드 조회
     *
     * * 메인페이지 카드에 노출할 오늘의 학습 영역, 문항 수,
     * 예상 소요 시간을 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 오늘의 학습 카드 응답 정보
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public TodayLearningResponseDto findTodayLearning(String loginId) {
        validateUser(loginId);

        // TODO: 학습 콘텐츠 API 구현 후 사용자 레벨과 오늘 날짜를 기준으로 실제 학습 데이터를 조회하도록 변경 예정
        return TodayLearningResponseDto.of(
                TODAY_LEARNING_CATEGORY,
                TODAY_LEARNING_TITLE,
                TODAY_LEARNING_TOTAL_QUESTION_COUNT,
                TODAY_LEARNING_ESTIMATED_MINUTES
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

    private void validateUser(String loginId) {
        findUserByLoginId(loginId);
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
}
