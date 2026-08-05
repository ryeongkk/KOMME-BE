package com.komme.domain.user.entity;

import com.komme.domain.user.enums.TermsType;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TermsAgreementTests {

    // 약관 동의 엔티티 동의 상태 생성 검증
    @Test
    void createStoresAgreedState() {
        User user = org.mockito.Mockito.mock(User.class);

        TermsAgreement termsAgreement = TermsAgreement.create(user, TermsType.MARKETING, true);

        assertThat(termsAgreement.getUser()).isSameAs(user);
        assertThat(termsAgreement.getTermsType()).isEqualTo(TermsType.MARKETING);
        assertThat(termsAgreement.isAgreed()).isTrue();
        assertThat(termsAgreement.getAgreedAt()).isNotNull();
    }

    // 약관 동의 해제 시각 초기화 검증
    @Test
    void updateAgreementClearsAgreedAtWhenDisagreed() {
        TermsAgreement termsAgreement = TermsAgreement.create(
                org.mockito.Mockito.mock(User.class),
                TermsType.PUSH_NOTIFICATION,
                true
        );

        termsAgreement.updateAgreement(false);

        assertThat(termsAgreement.isAgreed()).isFalse();
        assertThat(termsAgreement.getAgreedAt()).isNull();
    }
}
