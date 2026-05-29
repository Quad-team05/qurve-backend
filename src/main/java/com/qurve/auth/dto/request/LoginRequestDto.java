package com.qurve.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "필수로 입력해야 합니다.")
    @Size(min = 1, max = 30)
    private String loginId;

    @NotBlank(message = "필수로 입력해야 합니다.")
    @Size(min = 8, max = 20)
    private String password;
}
