package com.komme.domain.auth.dto.request;

import com.komme.domain.user.util.NicknamePolicy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OAuthProfileCompleteRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Pattern(regexp = NicknamePolicy.PATTERN, message = NicknamePolicy.MESSAGE)
        String nickname
) {
}
