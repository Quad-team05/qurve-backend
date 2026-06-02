package com.qurve.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequestDto {

    @Size(max = 20)
    private String name;

    @Size(max = 30)
    private String nickname;

    @Size(max = 255)
    private String learningGoal;

    @Size(max = 255)
    private Integer currentLevel;
}
