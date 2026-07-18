package com.komme.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateTermsAgreementRequest(
        @NotNull(message = "약관 동의 여부는 필수입니다.")
        Boolean agreed
) {
}
