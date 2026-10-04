package com.qurve.attendance.repository;

import com.qurve.attendance.domain.AttendanceRecord;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    boolean existsByUserAndLearningLanguageAndAttendanceDate(
            User user,
            LearningLanguage learningLanguage,
            LocalDate attendanceDate
    );

    @Query("""
            select ar.attendanceDate
            from AttendanceRecord ar
            where ar.user = :user
              and ar.learningLanguage = :learningLanguage
              and ar.attendanceDate between :startDate and :endDate
            """)
    List<LocalDate> findAttendanceDatesByUserAndLearningLanguageAndAttendanceDateBetween(
            @Param("user") User user,
            @Param("learningLanguage") LearningLanguage learningLanguage,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
