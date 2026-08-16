package com.qurve.attendance.repository;

import com.qurve.attendance.domain.StudyStatistics;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyStatisticsRepository extends JpaRepository<StudyStatistics, Long> {
    Optional<StudyStatistics> findByUser_UserId(Long userId);
    Optional<StudyStatistics> findByUser(User user);
}
