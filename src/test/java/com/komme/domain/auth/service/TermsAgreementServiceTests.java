package com.komme.domain.auth.service;

import com.komme.domain.auth.dto.request.TermsAgreementRequest;
import com.komme.domain.auth.entity.User;
import com.komme.domain.auth.enums.TermsType;
import com.komme.domain.auth.repository.TermsAgreementRepository;
import com.komme.domain.auth.repository.UserRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TermsAgreementServiceTests {

    private static final Long USER_ID = 1L;

    @Mock
    private TermsAgreementRepository termsAgreementRepository;

    @Mock
    private UserRepository userRepository;

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

    // 사용자 약관 동의 상태 일괄 저장 검증
    @Test
    void agreeSavesAllTermsForUser() {
        TermsAgreementService service = new TermsAgreementService(
                termsAgreementRepository,
                userReader
        );
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);

        service.agree(USER_ID, new TermsAgreementRequest(
                true,
                true,
                true,
                true,
                false,
                true,
                true
        ));

        verify(termsAgreementRepository, org.mockito.Mockito.times(7))
                .save(any());
    }
}
