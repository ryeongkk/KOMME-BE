package com.komme.domain.user.dto.request;

import com.komme.i18n.enums.Language;

import jakarta.validation.constraints.NotNull;

public record ChangePreferredLanguageRequest(
        @NotNull(message = "선호 언어는 필수입니다.")
        Language preferredLanguage
) {
}
