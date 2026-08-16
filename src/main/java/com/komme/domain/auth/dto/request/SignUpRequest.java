package com.komme.domain.auth.dto.request;

import com.komme.domain.auth.util.PasswordPolicy;
import com.komme.domain.user.util.NicknamePolicy;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = PasswordPolicy.PATTERN, message = PasswordPolicy.MESSAGE)
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Pattern(regexp = NicknamePolicy.PATTERN, message = NicknamePolicy.MESSAGE)
        String nickname
) {
}
