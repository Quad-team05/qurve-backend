package com.qurve.user.domain;

import com.qurve.global.entity.BaseEntity;
import com.qurve.global.enums.LearningLanguage;
import com.qurve.global.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 255, nullable = false, unique = true)
    private String loginId;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "learning_language", nullable = false)
    private LearningLanguage learningLanguage = LearningLanguage.JAPANESE;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "nickname", length = 100, nullable = false)
    private String nickname;

    @Column(name = "current_level_japanese")
    private Integer currentLevelJapanese;

    @Column(name = "current_level_english")
    private Integer currentLevelEnglish;

    @Column(name = "learning_goal", length = 255)
    private String learningGoal;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Column(name = "refresh_token_expired_at")
    private LocalDateTime refreshTokenExpiredAt;

    public void updateRefreshToken(String refreshToken, LocalDateTime expiredAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiredAt = expiredAt;
    }

    public void withdraw() {
        this.isDeleted = true;
    }

    public void updatePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

    // 현재 학습 언어 기준으로 레벨 갱신
    public void updateLevel(int level) {
        if (this.learningLanguage == LearningLanguage.ENGLISH) {
            this.currentLevelEnglish = level;
        } else {
            this.currentLevelJapanese = level;
        }
    }

    // 현재 학습 언어 기준 레벨 조회
    public Integer getCurrentLevel() {
        return this.learningLanguage == LearningLanguage.ENGLISH
                ? this.currentLevelEnglish
                : this.currentLevelJapanese;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiredAt = null;
    }

    public void updateLearningProfile(String learningGoal, Integer currentLevel) {
        this.learningGoal = learningGoal;
        updateLevel(currentLevel == null ? 0 : currentLevel);
    }

    public void updateProfile(String name, String nickname, String learningGoal, Integer currentLevel) {
        if (name != null) {
            this.name = name;
        }

        if (nickname != null) {
            this.nickname = nickname;
        }

        if (learningGoal != null) {
            this.learningGoal = learningGoal;
        }

        if (currentLevel != null) {
            updateLevel(currentLevel);
        }
    }

    public void updateLearningLanguage(LearningLanguage learningLanguage) {
        this.learningLanguage = learningLanguage;
    }
}