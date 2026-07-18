package com.komme.domain.user.service;

import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.enums.TermsType;
import com.komme.domain.auth.service.TermsAgreementService;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.entity.User;
import com.komme.i18n.enums.Language;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTests {

    private static final Long USER_ID = 1L;

    @Mock
    private UserReader userReader;

    @Mock
    private TermsAgreementService termsAgreementService;

    private UserProfileService userProfileService;

    // 사용자 프로필 서비스 테스트 환경 구성
    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService(
                userReader,
                termsAgreementService
        );
    }

    // 마이페이지 프로필 조회 응답 검증
    @Test
    void getMyProfileReturnsUserAndOptionalTermsAgreements() {
        User user = mock(User.class);
        when(user.getNickname()).thenReturn("nickname");
        when(user.getProvider()).thenReturn(Provider.LOCAL);
        when(user.getPreferredLanguage()).thenReturn(Language.JAPANESE);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);
        when(termsAgreementService.getOptionalConsentAgreements(USER_ID))
                .thenReturn(Map.of(
                        TermsType.MARKETING,
                        true,
                        TermsType.PUSH_NOTIFICATION,
                        false
                ));

        UserProfileResponse response = userProfileService.getMyProfile(USER_ID);

        assertThat(response.nickname()).isEqualTo("nickname");
        assertThat(response.provider()).isEqualTo(Provider.LOCAL);
        assertThat(response.preferredLanguage()).isEqualTo(Language.JAPANESE);
        assertThat(response.marketingAgreed()).isTrue();
        assertThat(response.pushNotificationAgreed()).isFalse();
    }
}
