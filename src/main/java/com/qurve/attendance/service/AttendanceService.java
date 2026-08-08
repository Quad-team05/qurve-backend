package com.qurve.attendance.service;

import com.qurve.attendance.domain.StudyStatistics;
import com.qurve.attendance.dto.response.AttendanceDayResponseDto;
import com.qurve.attendance.dto.response.AttendanceResponseDto;
import com.qurve.attendance.repository.StudyStatisticsRepository;
import com.qurve.badge.service.BadgeService;
import com.qurve.global.enums.ErrorCode;
import com.qurve.global.exception.BusinessException;
import com.qurve.user.domain.User;
import com.qurve.user.repository.UserRepository;
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
    private final StudyStatisticsRepository studyStatisticsRepository;
    private final BadgeService badgeService;

    /**
     * 출석 카드 조회
     *
     * * 연속 학습 일수(streak)와 마지막 갱신일(updated_at)을 기준으로
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
        boolean checkedToday = isCheckedToday(studyStatistics.getUpdatedAt(), today);

        return AttendanceResponseDto.from(
                studyStatistics.getStreakDays(),
                checkedToday,
                createAttendanceDays(studyStatistics.getStreakDays(), studyStatistics.getUpdatedAt(), today)
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
        int updatedStreakDays = calculateUpdatedStreakDays(
                studyStatistics.getStreakDays(),
                studyStatistics.getUpdatedAt(),
                today
        );

        studyStatistics.updateStreakDays(updatedStreakDays);
        badgeService.evaluate(user);

        return AttendanceResponseDto.from(
                updatedStreakDays,
                true,
                createAttendanceDays(updatedStreakDays, LocalDateTime.now(KST_ZONE), today)
        );
    }

    private User findUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private StudyStatistics findOrCreateStudyStatistics(User user) {
        return studyStatisticsRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> studyStatisticsRepository.save(StudyStatistics.create(user)));
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
