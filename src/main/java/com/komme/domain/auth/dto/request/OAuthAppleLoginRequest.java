package com.komme.domain.auth.dto.request;

import com.komme.domain.i18n.enums.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OAuthAppleLoginRequest(
        @NotBlank(message = "Apple identity token은 필수입니다.")
        String identityToken,

        @NotNull(message = "선호 언어는 필수입니다.")
        Language preferredLanguage
) {
}
