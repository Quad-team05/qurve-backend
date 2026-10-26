package com.qurve.attendance.repository;

import com.qurve.attendance.domain.DailyStudyLog;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyStudyLogRepository extends JpaRepository<DailyStudyLog, Long> {
    Optional<DailyStudyLog> findByUserAndStudyDate(User user, LocalDate studyDate);
}
