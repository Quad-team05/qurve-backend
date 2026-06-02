package com.qurve.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDto {

    @NotBlank(message = "필수로 입력해야 합니다.")
    @Size(min = 1, max = 30)
    private String loginId;

    @NotBlank(message = "필수로 입력해야 합니다.")
    @Email
    private String email;

    @NotBlank(message = "필수로 입력해야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String newPassword;
}
