package com.komme.domain.auth.service.oauth;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.entity.OAuthAccount;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.repository.OAuthAccountRepository;
import com.komme.domain.auth.service.AuthConstraintExceptionMapper;
import com.komme.domain.auth.service.token.WithdrawalStore;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.repository.UserRepository;
import com.komme.domain.user.service.UserReader;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthAccountServiceTests {

    private static final String PROVIDER_ID = "provider-id";
    private static final String EMAIL = "user@example.com";

    @Mock
    private OAuthAccountRepository oAuthAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserReader userReader;

    @Mock
    private WithdrawalStore withdrawalStore;

    private OAuthAccountService oAuthAccountService;

    // OAuth 계정 서비스 테스트 환경 구성
    @BeforeEach
    void setUp() {
        oAuthAccountService = new OAuthAccountService(
                oAuthAccountRepository,
                userRepository,
                userReader,
                withdrawalStore,
                new AuthConstraintExceptionMapper()
        );
    }

    // 연결된 OAuth 계정 사용자 반환 검증
    @Test
    void resolveUserReturnsLinkedUser() {
        User user = createUserMock();
        OAuthAccount account = OAuthAccount.create(user, Provider.GOOGLE, PROVIDER_ID);
        when(oAuthAccountRepository.findByProviderAndProviderId(Provider.GOOGLE, PROVIDER_ID))
                .thenReturn(Optional.of(account));

        User resolvedUser = oAuthAccountService.resolveUser(Provider.GOOGLE, PROVIDER_ID, null);

        assertThat(resolvedUser).isSameAs(user);
        verify(userReader, never()).findByEmail(any());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    // 기존 이메일 사용자 OAuth 계정 연결 검증
    @Test
    void resolveUserLinksExistingEmailUser() {
        User user = createUserMock();
        when(userReader.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        User resolvedUser = oAuthAccountService.resolveUser(
                Provider.GOOGLE,
                PROVIDER_ID,
                " USER@Example.COM "
        );

        ArgumentCaptor<OAuthAccount> accountCaptor = ArgumentCaptor.forClass(OAuthAccount.class);
        verify(oAuthAccountRepository).saveAndFlush(accountCaptor.capture());
        assertThat(resolvedUser).isSameAs(user);
        assertThat(accountCaptor.getValue().getUser()).isSameAs(user);
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo(Provider.GOOGLE);
        assertThat(accountCaptor.getValue().getProviderId()).isEqualTo(PROVIDER_ID);
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    // 신규 OAuth 사용자 생성 및 계정 연결 검증
    @Test
    void resolveUserCreatesOAuthUser() {
        User savedUser = createUserMock();
        when(userReader.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);

        User resolvedUser = oAuthAccountService.resolveUser(Provider.APPLE, PROVIDER_ID, EMAIL);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(withdrawalStore).validateNotWithdrawn(EMAIL);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        verify(oAuthAccountRepository).saveAndFlush(any(OAuthAccount.class));
        assertThat(resolvedUser).isSameAs(savedUser);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(userCaptor.getValue().getProvider()).isEqualTo(Provider.APPLE);
    }

    // Google 신규 계정 이메일 누락 거부 검증
    @Test
    void resolveUserRejectsMissingGoogleEmail() {
        assertThatThrownBy(() -> oAuthAccountService.resolveUser(
                Provider.GOOGLE,
                PROVIDER_ID,
                null
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.GOOGLE_EMAIL_REQUIRED);
    }

    // OAuth 계정 연결 제약조건 충돌 오류 변환 검증
    @Test
    void resolveUserMapsOAuthAccountConflict() {
        User user = createUserMock();
        when(userReader.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(oAuthAccountRepository.saveAndFlush(any(OAuthAccount.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate oauth account"));

        assertThatThrownBy(() -> oAuthAccountService.resolveUser(
                Provider.APPLE,
                PROVIDER_ID,
                EMAIL
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.OAUTH_ACCOUNT_ALREADY_LINKED);
    }

    // 테스트 사용자 Mock 생성
    private User createUserMock() {
        return org.mockito.Mockito.mock(User.class);
    }
}
