package com.qurve.user.dto.response;

import com.qurve.global.enums.Role;
import com.qurve.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponseDto {
    private Long userId;
    private String loginId;
    private String email;
    private String name;
    private String nickname;
    private String learningGoal;
    private String currentLevel;
    private boolean emailVerified;
    private Role role;
    private LocalDateTime createdAt;

    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .learningGoal(user.getLearningGoal())
                .currentLevel(user.getCurrentLevel())
                .emailVerified(user.isEmailVerified())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
