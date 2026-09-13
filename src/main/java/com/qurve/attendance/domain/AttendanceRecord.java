package com.qurve.attendance.domain;

import com.qurve.global.enums.LearningLanguage;
import com.qurve.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "tb_attendance_record",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_record_user_language_date",
                        columnNames = {"user_id", "learning_language", "attendance_date"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_attendance_record_user_language_date",
                        columnList = "user_id, learning_language, attendance_date"
                )
        }
)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_record_id")
    private Long attendanceRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_language", nullable = false, length = 20)
    private LearningLanguage learningLanguage;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    private AttendanceRecord(User user, LearningLanguage learningLanguage, LocalDate attendanceDate) {
        this.user = user;
        this.learningLanguage = learningLanguage;
        this.attendanceDate = attendanceDate;
    }

    public static AttendanceRecord create(User user, LearningLanguage learningLanguage, LocalDate attendanceDate) {
        return new AttendanceRecord(user, learningLanguage, attendanceDate);
    }
}
