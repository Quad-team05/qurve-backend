package com.qurve.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FindIdRequestDto {

    @NotBlank(message = "필수로 입력해야 합니다.")
    @Size(max = 20)
    private String name;

    @NotBlank(message = "필수로 입력해야 합니다.")
    @Email
    private String email;
}
