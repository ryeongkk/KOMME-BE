package com.komme.domain.auth.dto.request;

import com.komme.domain.auth.util.PasswordPolicy;
import com.komme.domain.user.enums.Gender;
import com.komme.domain.user.enums.ServiceInterest;
import com.komme.domain.user.util.NicknamePolicy;
import com.komme.i18n.enums.Language;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
        String nickname,

        @NotBlank(message = "국적은 필수입니다.")
        @Pattern(regexp = "(?i)^[a-z]{2}$", message = "국적은 ISO 2자리 국가 코드여야 합니다.")
        String nationality,

        @NotNull(message = "성별은 필수입니다.")
        Gender gender,

        @NotNull(message = "선호 언어는 필수입니다.")
        Language preferredLanguage,

        @NotEmpty(message = "관심 서비스는 하나 이상 선택해야 합니다.")
        Set<@NotNull(message = "관심 서비스 값은 null일 수 없습니다.") ServiceInterest> serviceInterests
) {
}
