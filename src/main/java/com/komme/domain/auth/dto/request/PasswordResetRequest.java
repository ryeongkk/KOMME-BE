package com.komme.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetRequest(
        @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다.")
        String resetToken,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[!-~]{8,20}$",
                message = "새 비밀번호는 8~20자의 영문과 숫자를 포함해야 합니다."
        )
        String newPassword
) {
}
