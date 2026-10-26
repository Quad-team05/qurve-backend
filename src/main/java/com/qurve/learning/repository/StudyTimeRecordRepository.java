package com.qurve.learning.repository;

import com.qurve.learning.domain.StudyTimeRecord;
import com.qurve.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyTimeRecordRepository extends JpaRepository<StudyTimeRecord, Long> {
    Optional<StudyTimeRecord> findByUserAndStudyDate(User user, LocalDate studyDate);
    List<StudyTimeRecord> findAllByUserAndStudyDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
