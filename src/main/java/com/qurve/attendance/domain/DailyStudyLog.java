package com.qurve.attendance.domain;

import com.qurve.global.entity.BaseEntity;
import com.qurve.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_daily_study_log",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_daily_study_log_user_date", columnNames = {"user_id", "study_date"})
        },
        indexes = {
                @Index(name = "idx_daily_study_log_user_date", columnList = "user_id, study_date")
        }
)
public class DailyStudyLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_study_log_id")
    private Long dailyStudyLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Builder.Default
    @Column(name = "study_time_minutes", nullable = false)
    private int studyTimeMinutes = 0;

    public static DailyStudyLog create(User user, LocalDate studyDate) {
        return DailyStudyLog.builder()
                .user(user)
                .studyDate(studyDate)
                .build();
    }

    public void addStudyTime(int studyTimeMinutes) {
        this.studyTimeMinutes += studyTimeMinutes;
    }
}
