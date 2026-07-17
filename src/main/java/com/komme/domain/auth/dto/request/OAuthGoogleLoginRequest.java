package com.komme.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthGoogleLoginRequest(
        @NotBlank(message = "Google ID token은 필수입니다.")
        String idToken
) {
}
