package com.komme.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record LocationConsentRequest(
        @NotNull(message = "위치 정보 동의 여부는 필수입니다.")
        Boolean agreed
) {
}
