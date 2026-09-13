package com.qurve.attendance.repository;

import com.qurve.attendance.domain.AttendanceRecord;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    boolean existsByUserAndLearningLanguageAndAttendanceDate(
            User user,
            LearningLanguage learningLanguage,
            LocalDate attendanceDate
    );
}
