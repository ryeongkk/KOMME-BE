package com.komme.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthAppleLoginRequest(
        @NotBlank(message = "Apple identity token은 필수입니다.")
        String identityToken
) {
}
