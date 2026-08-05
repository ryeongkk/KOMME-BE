package com.komme.domain.user.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.user.entity.TermsAgreement;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.TermsType;
import com.komme.domain.user.exception.UserErrorStatus;
import com.komme.domain.user.repository.TermsAgreementRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TermsAgreementServiceTests {

    private static final Long USER_ID = 1L;

    @Mock
    private TermsAgreementRepository termsAgreementRepository;

    @Mock
    private UserReader userReader;

    // 필수 약관 전체 동의 상태 조회 검증
    @Test
    void areRequiredTermsAgreedReturnsTrueWhenAllRequiredTermsAreAgreed() {
        TermsAgreementService service = new TermsAgreementService(
                termsAgreementRepository,
                userReader
        );
        when(termsAgreementRepository.countByUserIdAndTermsTypeInAndAgreedTrue(
                USER_ID,
                TermsType.requiredTypes()
        )).thenReturn((long) TermsType.requiredTypes().size());

        assertThat(service.areRequiredTermsAgreed(USER_ID)).isTrue();
    }

    // 필수 약관 일부 미동의 상태 조회 검증
    @Test
    void areRequiredTermsAgreedReturnsFalseWhenAnyRequiredTermsAreMissing() {
        TermsAgreementService service = new TermsAgreementService(
                termsAgreementRepository,
                userReader
        );
        when(termsAgreementRepository.countByUserIdAndTermsTypeInAndAgreedTrue(
                USER_ID,
                TermsType.requiredTypes()
        )).thenReturn((long) TermsType.requiredTypes().size() - 1);

        assertThat(service.areRequiredTermsAgreed(USER_ID)).isFalse();
    }

    // 사용자 약관 동의 상태 일괄 저장 검증
    @Test
    void agreeSavesAllTermsForUser() {
        TermsAgreementService service = new TermsAgreementService(
                termsAgreementRepository,
                userReader
        );
        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);

        service.agree(USER_ID, Map.of(
                TermsType.SERVICE_TERMS, true,
                TermsType.PRIVACY_POLICY, true,
                TermsType.LOCATION_TERMS, true,
                TermsType.LOCATION_COLLECTION, true,
                TermsType.MARKETING, false,
                TermsType.PUSH_NOTIFICATION, true,
                TermsType.AGE_CONFIRMATION, true
        ));

        verify(termsAgreementRepository, times(7))
                .save(any());
    }

    // 사용자 선택 약관 동의 상태 조회 검증
    @Test
    void getOptionalConsentAgreementsReturnsSavedAndDefaultValues() {
        TermsAgreementService service = new TermsAgreementService(
                termsAgreementRepository,
                userReader
        );
        TermsAgreement marketing = mock(TermsAgreement.class);
        when(marketing.getTermsType()).thenReturn(TermsType.MARKETING);
        when(marketing.isAgreed()).thenReturn(true);
        when(termsAgreementRepository.findByUserIdAndTermsTypeIn(
                USER_ID,
                TermsType.optionalConsentTypes()
        )).thenReturn(List.of(marketing));

        Map<TermsType, Boolean> result = service.getOptionalConsentAgreements(USER_ID);

        assertThat(result.get(TermsType.MARKETING)).isTrue();
        assertThat(result.get(TermsType.PUSH_NOTIFICATION)).isFalse();
    }

    // 사용자 선택 약관 동의 상태 변경 검증
    @Test
    void updateOptionalConsentSavesOptionalTermsAgreement() {
        TermsAgreementService service = new TermsAgreementService(
                termsAgreementRepository,
                userReader
        );
        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);

        service.updateOptionalConsent(USER_ID, TermsType.MARKETING, true);

        verify(termsAgreementRepository).save(any());
    }

    // 기존 선택 약관 동의 상태 갱신 검증
    @Test
    void updateOptionalConsentUpdatesExistingTermsAgreement() {
        TermsAgreementService service = new TermsAgreementService(
                termsAgreementRepository,
                userReader
        );
        User user = mock(User.class);
        TermsAgreement termsAgreement = TermsAgreement.create(
                user,
                TermsType.PUSH_NOTIFICATION,
                false
        );
        when(user.getId()).thenReturn(USER_ID);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);
        when(termsAgreementRepository.findByUserIdAndTermsType(
                USER_ID,
                TermsType.PUSH_NOTIFICATION
        )).thenReturn(Optional.of(termsAgreement));

        service.updateOptionalConsent(USER_ID, TermsType.PUSH_NOTIFICATION, true);

        assertThat(termsAgreement.isAgreed()).isTrue();
        assertThat(termsAgreement.getAgreedAt()).isNotNull();
        verify(termsAgreementRepository).save(termsAgreement);
    }

    // 필수 약관 개별 변경 거부 검증
    @Test
    void updateOptionalConsentRejectsRequiredTermsType() {
        TermsAgreementService service = new TermsAgreementService(
                termsAgreementRepository,
                userReader
        );

        assertThatThrownBy(() -> service.updateOptionalConsent(
                USER_ID,
                TermsType.SERVICE_TERMS,
                true
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(UserErrorStatus.UNSUPPORTED_TERMS_TYPE);

        verify(termsAgreementRepository, never()).save(any());
    }

}
