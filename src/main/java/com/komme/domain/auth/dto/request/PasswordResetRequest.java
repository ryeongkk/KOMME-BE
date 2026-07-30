package com.komme.domain.auth.dto.request;

import com.komme.domain.auth.util.PasswordPolicy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetRequest(
        @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다.")
        String resetToken,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(regexp = PasswordPolicy.PATTERN, message = PasswordPolicy.MESSAGE)
        String newPassword
) {
}
