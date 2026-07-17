package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.client.OAuthAppleClient;
import com.komme.domain.auth.client.OAuthIdentity;
import com.komme.domain.auth.client.OAuthGoogleClient;
import com.komme.domain.auth.dto.request.OAuthAppleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthGoogleLoginRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.entity.OAuthAccount;
import com.komme.domain.auth.entity.User;
import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.repository.OAuthAccountRepository;
import com.komme.domain.auth.repository.UserRepository;

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
class OAuthServiceTests {

    private static final Long USER_ID = 1L;
    private static final String IDENTITY_TOKEN = "identity-token";
    private static final String APPLE_SUBJECT = "apple-sub";
    private static final String EMAIL = "user@example.com";

    @Mock
    private OAuthAppleClient oAuthAppleClient;

    @Mock
    private OAuthGoogleClient oAuthGoogleClient;

    @Mock
    private OAuthAccountRepository oAuthAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenService authTokenService;

    private OAuthService oAuthService;

    // Apple OAuth 서비스 테스트 환경 구성
    @BeforeEach
    void setUp() {
        oAuthService = new OAuthService(
                oAuthAppleClient,
                oAuthGoogleClient,
                new OAuthAccountService(oAuthAccountRepository, userRepository),
                authTokenService
        );
    }

    // 연결된 Apple 계정 로그인 검증
    @Test
    void loginWithAppleLogsInLinkedUserWithoutEmail() {
        User user = createUserMock();
        OAuthAccount account = OAuthAccount.create(user, Provider.APPLE, APPLE_SUBJECT);
        prepareIdentity(null);
        when(oAuthAccountRepository.findByProviderAndProviderId(
                Provider.APPLE,
                APPLE_SUBJECT
        )).thenReturn(Optional.of(account));
        prepareTokenResponse();

        LoginResponse response = oAuthService.loginWithApple(createRequest());

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(userRepository, never()).findByEmail(any());
        verify(authTokenService).issueLoginTokens(USER_ID);
    }

    // 동일 이메일 LOCAL 사용자 Apple 계정 연결 검증
    @Test
    void loginWithAppleLinksExistingLocalUser() {
        User user = createUserMock();
        prepareIdentity(" USER@example.com ");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        prepareTokenResponse();

        oAuthService.loginWithApple(createRequest());

        ArgumentCaptor<OAuthAccount> accountCaptor =
                ArgumentCaptor.forClass(OAuthAccount.class);
        verify(oAuthAccountRepository).saveAndFlush(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUser()).isSameAs(user);
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo(Provider.APPLE);
        assertThat(accountCaptor.getValue().getProviderId()).isEqualTo(APPLE_SUBJECT);
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    // 신규 Apple 사용자 최소 프로필 생성 검증
    @Test
    void loginWithAppleCreatesNewAppleUser() {
        User savedUser = createUserMock();
        prepareIdentity(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);
        prepareTokenResponse();

        oAuthService.loginWithApple(createRequest());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User newUser = userCaptor.getValue();
        assertThat(newUser.getEmail()).isEqualTo(EMAIL);
        assertThat(newUser.getProvider()).isEqualTo(Provider.APPLE);
        assertThat(newUser.getNickname()).isNull();
        verify(oAuthAccountRepository).saveAndFlush(any(OAuthAccount.class));
    }

    // 이메일 없는 미연결 Apple 계정 로그인 거부 검증
    @Test
    void loginWithAppleRejectsUnknownAccountWithoutEmail() {
        prepareIdentity(null);

        assertThatThrownBy(() -> oAuthService.loginWithApple(createRequest()))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.APPLE_EMAIL_REQUIRED);

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(authTokenService, never()).issueLoginTokens(any());
    }

    // 연결된 Google 계정 로그인 검증
    @Test
    void loginWithGoogleLogsInLinkedUser() {
        User user = createUserMock();
        OAuthAccount account = OAuthAccount.create(user, Provider.GOOGLE, APPLE_SUBJECT);
        when(oAuthGoogleClient.verifyIdentityToken("google-id-token"))
                .thenReturn(new OAuthIdentity(APPLE_SUBJECT, EMAIL));
        when(oAuthAccountRepository.findByProviderAndProviderId(
                Provider.GOOGLE,
                APPLE_SUBJECT
        )).thenReturn(Optional.of(account));
        prepareTokenResponse();

        LoginResponse response = oAuthService.loginWithGoogle(
                new OAuthGoogleLoginRequest("google-id-token")
        );

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(authTokenService).issueLoginTokens(USER_ID);
    }

    // 동시 OAuth 이메일 생성 충돌 도메인 오류 변환 검증
    @Test
    void loginWithGoogleMapsConcurrentEmailConflict() {
        when(oAuthGoogleClient.verifyIdentityToken("google-id-token"))
                .thenReturn(new OAuthIdentity(APPLE_SUBJECT, EMAIL));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThatThrownBy(() -> oAuthService.loginWithGoogle(
                new OAuthGoogleLoginRequest("google-id-token")
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_ALREADY_EXISTS);

        verify(authTokenService, never()).issueLoginTokens(any());
    }

    // Apple identity token 검증 결과 구성
    private void prepareIdentity(String email) {
        when(oAuthAppleClient.verifyIdentityToken(IDENTITY_TOKEN))
                .thenReturn(new OAuthIdentity(APPLE_SUBJECT, email));
    }

    // 로그인 토큰 응답 구성
    private void prepareTokenResponse() {
        when(authTokenService.issueLoginTokens(USER_ID))
                .thenReturn(LoginResponse.of("access-token", "refresh-token"));
    }

    // Apple 로그인 요청 생성
    private OAuthAppleLoginRequest createRequest() {
        return new OAuthAppleLoginRequest(IDENTITY_TOKEN);
    }

    // 사용자 Mock 생성
    private User createUserMock() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
        return user;
    }
}
