package com.qurve.attendance.domain;

import com.qurve.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_study_statistics")
@EntityListeners(AuditingEntityListener.class)
public class StudyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistics_id")
    private Long statisticsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "total_study_time", nullable = false)
    private int totalStudyTime = 0;

    @Builder.Default
    @Column(name = "total_solved_quiz", nullable = false)
    private int totalSolvedQuiz = 0;

    @Builder.Default
    @Column(name = "average_accuracy", nullable = false)
    private int averageAccuracy = 0;

    @Builder.Default
    @Column(name = "streak_days", nullable = false)
    private int streakDays = 0;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static StudyStatistics create(User user) {
        return StudyStatistics.builder()
                .user(user)
                .build();
    }

    public void updateStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }
}
