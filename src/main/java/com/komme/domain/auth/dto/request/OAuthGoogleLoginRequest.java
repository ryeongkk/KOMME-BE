package com.komme.domain.auth.dto.request;

import com.komme.domain.i18n.enums.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OAuthGoogleLoginRequest(
        @NotBlank(message = "Google authorization code는 필수입니다.")
        String code,

        @NotNull(message = "선호 언어는 필수입니다.")
        Language preferredLanguage
) {
}
