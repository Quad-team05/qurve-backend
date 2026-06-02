package com.qurve.user.domain;

import com.qurve.global.entity.BaseEntity;
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

    @Column(name = "login_id", length = 30, nullable = false, unique = true)
    private String loginId;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "nickname", length = 30, nullable = false)
    private String nickname;

    @Column(name = "current_level")
    private Integer currentLevel;

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

    public void updateLevel(int level) {
        this.currentLevel = level;
    }
  
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiredAt = null;
    }

    public void updateLearningProfile(String learningGoal, Integer currentLevel) {
        this.learningGoal = learningGoal;
        this.currentLevel = currentLevel;
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
            this.currentLevel = currentLevel;
        }
    }
}
