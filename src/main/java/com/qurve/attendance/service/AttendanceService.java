package com.qurve.attendance.service;

import com.qurve.attendance.domain.DailyStudyLog;
import com.qurve.attendance.domain.StudyStatistics;
import com.qurve.attendance.dto.request.StudyTimeSaveRequestDto;
import com.qurve.attendance.dto.response.AttendanceDayResponseDto;
import com.qurve.attendance.dto.response.AttendanceResponseDto;
import com.qurve.attendance.dto.response.StudyTimeSaveResponseDto;
import com.qurve.attendance.repository.DailyStudyLogRepository;
import com.qurve.attendance.repository.StudyStatisticsRepository;
import com.qurve.badge.service.BadgeService;
import com.qurve.challenge.domain.ChallengeGoalType;
import com.qurve.challenge.service.ChallengeProgressService;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.enums.XpActionType;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
import com.qurve.xp.service.XpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final List<String> DAY_OF_WEEK_LABELS = List.of("월", "화", "수", "목", "금", "토", "일");

    private final UserRepository userRepository;
    private final DailyStudyLogRepository dailyStudyLogRepository;
    private final StudyStatisticsRepository studyStatisticsRepository;
    private final BadgeService badgeService;
    private final XpService xpService;
    private final ChallengeProgressService challengeProgressService;

    /**
     * 출석 카드 조회
     *
     * * 연속 학습 일수(streak)와 마지막 출석일(lastAttendanceAt)을 기준으로
     * 현재 주차(월~일) 출석 활성화 상태를 계산해 반환한다.
     *
     * @param loginId 로그인 ID
     * @return 출석 카드 응답 정보
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    public AttendanceResponseDto findOne(String loginId) {
        User user = findUserByLoginId(loginId);
        StudyStatistics studyStatistics = findOrCreateStudyStatistics(user);

        LocalDate today = LocalDate.now(KST_ZONE);
        LocalDateTime lastAttendanceAt = resolveLastAttendanceAt(studyStatistics);
        boolean checkedToday = isCheckedToday(lastAttendanceAt, today);

        return AttendanceResponseDto.from(
                studyStatistics.getStreakDays(),
                checkedToday,
                createAttendanceDays(studyStatistics.getStreakDays(), lastAttendanceAt, today)
        );
    }

    /**
     * 출석 체크 저장
     *
     * * 마지막 출석일이 오늘이면 streak를 유지하고,
     * 마지막 출석일이 어제면 streak를 1 증가,
     * 그 외에는 streak를 1로 초기화
     *
     * @param loginId 로그인 ID
     * @return 갱신된 출석 카드 응답 정보
     * @throws BusinessException 유저가 존재하지 않는 경우
     */
    @Transactional
    public AttendanceResponseDto save(String loginId) {
        User user = findUserByLoginId(loginId);

        StudyStatistics studyStatistics = findOrCreateStudyStatistics(user);

        LocalDate today = LocalDate.now(KST_ZONE);
        LocalDateTime lastAttendanceAt = resolveLastAttendanceAt(studyStatistics);

        boolean alreadyCheckedToday = isCheckedToday(lastAttendanceAt, today);

        int updatedStreakDays = calculateUpdatedStreakDays(
                studyStatistics.getStreakDays(),
                lastAttendanceAt,
                today
        );

        LocalDateTime attendedAt = LocalDateTime.now(KST_ZONE);
        studyStatistics.updateAttendance(updatedStreakDays, attendedAt);

        if (!alreadyCheckedToday) {
            xpService.grantXp(user, XpActionType.DAILY_ATTENDANCE);
            if (updatedStreakDays == 3)
                xpService.grantXp(user, XpActionType.STREAK_3_DAYS);
            if (updatedStreakDays == 7)
                xpService.grantXp(user, XpActionType.STREAK_7_DAYS);
            challengeProgressService.addProgress(user, ChallengeGoalType.ATTENDANCE, 1);
        }

        badgeService.evaluate(user);

        return AttendanceResponseDto.from(
                updatedStreakDays,
                true,
                createAttendanceDays(updatedStreakDays, attendedAt, today)
        );
    }

    /**
     * 학습 시간 저장
     *
     * * 클라이언트에서 측정한 학습 시간을 분 단위로 전달받아
     * 사용자의 날짜별 학습 로그를 생성하거나 누적하고,
     * 전체 누적 학습 시간(totalStudyTime)에도 함께 더한다.
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
        LocalDate today = LocalDate.now(KST_ZONE);
        DailyStudyLog dailyStudyLog = findOrCreateDailyStudyLog(user, today);

        int studyTimeMinutes = requestDto.getStudyTimeMinutes();
        initializeLastAttendanceAtIfNeeded(studyStatistics);
        dailyStudyLog.addStudyTime(studyTimeMinutes);
        studyStatistics.addStudyTime(studyTimeMinutes);
        challengeProgressService.addProgress(user, ChallengeGoalType.STUDY_TIME, studyTimeMinutes);
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

    private DailyStudyLog findOrCreateDailyStudyLog(User user, LocalDate studyDate) {
        return dailyStudyLogRepository.findByUserAndStudyDate(user, studyDate)
                .orElseGet(() -> dailyStudyLogRepository.save(DailyStudyLog.create(user, studyDate)));
    }

    private LocalDateTime resolveLastAttendanceAt(StudyStatistics studyStatistics) {
        if (studyStatistics.getLastAttendanceAt() != null) {
            return studyStatistics.getLastAttendanceAt();
        }

        if (studyStatistics.getStreakDays() <= 0) {
            return null;
        }

        return studyStatistics.getUpdatedAt();
    }

    private void initializeLastAttendanceAtIfNeeded(StudyStatistics studyStatistics) {
        if (studyStatistics.getStreakDays() > 0 && studyStatistics.getUpdatedAt() != null) {
            studyStatistics.initializeLastAttendanceAt(studyStatistics.getUpdatedAt());
        }
    }

    private boolean isCheckedToday(LocalDateTime updatedAt, LocalDate today) {
        if (updatedAt == null) {
            return false;
        }

        return updatedAt.toLocalDate().isEqual(today);
    }

    private int calculateUpdatedStreakDays(int streakDays, LocalDateTime updatedAt, LocalDate today) {
        if (updatedAt == null || streakDays <= 0) {
            return 1;
        }

        LocalDate lastUpdatedDate = updatedAt.toLocalDate();

        if (lastUpdatedDate.isEqual(today)) {
            return streakDays;
        }

        if (lastUpdatedDate.plusDays(1).isEqual(today)) {
            return streakDays + 1;
        }

        return 1;
    }

    private List<AttendanceDayResponseDto> createAttendanceDays(
            int streakDays,
            LocalDateTime updatedAt,
            LocalDate today
    ) {
        List<AttendanceDayResponseDto> days = new ArrayList<>();

        LocalDate weekStartDate = today.with(DayOfWeek.MONDAY);
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        LocalDate streakEndDate = updatedAt == null ? null : updatedAt.toLocalDate();
        LocalDate streakStartDate =
                streakEndDate == null ? null : streakEndDate.minusDays(Math.max(streakDays - 1L, 0L));

        for (int i = 0; i < 7; i++) {
            LocalDate targetDate = weekStartDate.plusDays(i);

            boolean checked = streakStartDate != null
                    && !targetDate.isBefore(streakStartDate)
                    && !targetDate.isAfter(streakEndDate)
                    && !targetDate.isAfter(weekEndDate);

            days.add(AttendanceDayResponseDto.of(DAY_OF_WEEK_LABELS.get(i), checked));
        }

        return days;
    }
}
