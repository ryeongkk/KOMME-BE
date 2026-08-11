package com.komme.domain.user.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TermsTypeTests {

    // 선택 동의 약관 유형 집합 검증
    @Test
    void optionalConsentTypesContainsOptionalTermsOnly() {
        assertThat(TermsType.optionalConsentTypes())
                .containsExactlyInAnyOrder(
                        TermsType.MARKETING,
                        TermsType.PUSH_NOTIFICATION
                );
    }

    // 선택 동의 약관 유형 여부 검증
    @Test
    void isOptionalConsentTypeReturnsTypeClassification() {
        assertThat(TermsType.MARKETING.isOptionalConsentType()).isTrue();
        assertThat(TermsType.PUSH_NOTIFICATION.isOptionalConsentType()).isTrue();
        assertThat(TermsType.SERVICE_TERMS.isOptionalConsentType()).isFalse();
    }
}
