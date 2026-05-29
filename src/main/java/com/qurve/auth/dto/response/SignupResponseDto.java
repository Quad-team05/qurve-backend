package com.qurve.auth.dto.response;

import com.qurve.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponseDto {
    private Long userId;
    private String loginId;
    private String email;
    private String name;
    private String nickname;

    public static SignupResponseDto from(User user) {
        return SignupResponseDto.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .build();
    }
}
