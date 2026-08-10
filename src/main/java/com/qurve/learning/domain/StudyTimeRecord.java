package com.qurve.learning.domain;

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
        name = "tb_study_time_record",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_study_time_record_user_date", columnNames = {"user_id", "study_date"})
        },
        indexes = {
                @Index(name = "idx_study_time_record_user_date", columnList = "user_id, study_date")
        }
)
public class StudyTimeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Builder.Default
    @Column(name = "study_time_minutes", nullable = false)
    private int studyTimeMinutes = 0;

    public void addStudyTime(int studyTimeMinutes) {
        this.studyTimeMinutes += studyTimeMinutes;
    }
}
