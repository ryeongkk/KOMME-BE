package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.dto.response.TokenReissueResponse;
import com.komme.domain.auth.entity.User;
import com.komme.domain.auth.enums.Gender;
import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.enums.ServiceInterest;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.jwt.JwtRedisKeys;
import com.komme.domain.auth.repository.UserRepository;
import com.komme.i18n.enums.Language;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@example.com";
    private static final Duration ACCESS_EXPIRATION = Duration.ofHours(1);
    private static final Duration REFRESH_EXPIRATION = Duration.ofDays(14);

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private AuthService authService;

    // 인증 서비스 테스트 환경 구성
    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                emailVerificationService,
                passwordEncoder,
                jwtProvider,
                authTokenService,
                new RefreshTokenStore(redisTemplate, userRepository),
                new AccessTokenBlacklistStore(redisTemplate)
        );
    }

    // 회원가입 정규화와 LOCAL 사용자 저장 검증
    @Test
    void signUpStoresNormalizedLocalUser() {
        when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
        SignUpRequest request = createSignUpRequest();

        authService.signUp(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getNickname()).isEqualTo("nickname");
        assertThat(savedUser.getNationality()).isEqualTo("KR");
        assertThat(savedUser.getProvider()).isEqualTo(Provider.LOCAL);
        assertThat(savedUser.getPreferredLanguage()).isEqualTo(Language.ENGLISH);
        assertThat(savedUser.getServiceInterests()).containsExactly(ServiceInterest.COURSE);
        verify(emailVerificationService).validateVerifiedEmail(EMAIL);
        verify(emailVerificationService).deleteVerifiedEmail(EMAIL);
    }

    // 중복 이메일 회원가입 거부 검증
    @Test
    void signUpRejectsDuplicateEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(createSignUpRequest()))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    // 중복 닉네임 회원가입 거부 검증
    @Test
    void signUpRejectsDuplicateNickname() {
        when(userRepository.existsByNickname("nickname")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(createSignUpRequest()))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.NICKNAME_ALREADY_EXISTS);

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    // 동시 요청 이메일 unique 충돌 도메인 오류 변환 검증
    @Test
    void signUpMapsEmailUniqueConstraintViolation() {
        when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
        DataIntegrityViolationException exception = createUniqueConstraintException(
                "uk_user_email"
        );
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(exception);

        assertThatThrownBy(() -> authService.signUp(createSignUpRequest()))
                .isInstanceOf(GeneralException.class)
                .extracting(error -> ((GeneralException) error).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
    }

    // 동시 요청 닉네임 unique 충돌 도메인 오류 변환 검증
    @Test
    void signUpMapsNicknameUniqueConstraintViolation() {
        when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
        DataIntegrityViolationException exception = createUniqueConstraintException(
                "uk_user_nickname"
        );
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(exception);

        assertThatThrownBy(() -> authService.signUp(createSignUpRequest()))
                .isInstanceOf(GeneralException.class)
                .extracting(error -> ((GeneralException) error).getErrorStatus())
                .isEqualTo(AuthErrorStatus.NICKNAME_ALREADY_EXISTS);
    }

    // 로그인 토큰 발급과 Refresh Token 저장 검증
    @Test
    void loginIssuesAndStoresTokens() {
        User user = createLocalUserMock();
        when(user.getId()).thenReturn(USER_ID);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "encoded-password")).thenReturn(true);
        when(authTokenService.issueLoginTokens(USER_ID))
                .thenReturn(LoginResponse.of("access-token", "refresh-token"));

        LoginResponse response = authService.login(
                new LoginRequest(" USER@example.com ", "password1")
        );

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(authTokenService).issueLoginTokens(USER_ID);
    }

    // 잘못된 로그인 비밀번호 거부 검증
    @Test
    void loginRejectsInvalidPassword() {
        User user = createLocalUserMock();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest(EMAIL, "wrong-password")
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_CREDENTIALS);
    }

    // Refresh Token rotation 재발급 검증
    @Test
    void reissueTokenConsumesOldTokenAndStoresNewToken() {
        prepareRedisOperations();
        TokenClaims oldClaims = new TokenClaims(
                USER_ID,
                "old-refresh-id",
                Instant.now().plus(REFRESH_EXPIRATION)
        );
        when(jwtProvider.parseRefreshToken("old-refresh-token")).thenReturn(oldClaims);
        when(valueOperations.getAndDelete(JwtRedisKeys.refreshToken("old-refresh-id")))
                .thenReturn(USER_ID.toString());
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(authTokenService.issueLoginTokens(USER_ID))
                .thenReturn(LoginResponse.of("new-access-token", "new-refresh-token"));

        TokenReissueResponse response = authService.reissueToken(
                new TokenReissueRequest("old-refresh-token")
        );

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        verify(setOperations).remove(
                JwtRedisKeys.userRefreshTokens(USER_ID),
                "old-refresh-id"
        );
        verify(authTokenService).issueLoginTokens(USER_ID);
    }

    // 비밀번호 변경과 전체 Refresh Token 폐기 검증
    @Test
    @SuppressWarnings("unchecked")
    void changePasswordUpdatesPasswordAndDeletesRefreshTokens() {
        prepareSetOperations();
        User user = createLocalUserMock();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("newpassword2")).thenReturn("new-encoded-password");
        when(setOperations.members(JwtRedisKeys.userRefreshTokens(USER_ID)))
                .thenReturn(Set.of("refresh-id-1", "refresh-id-2"));

        authService.changePassword(
                USER_ID,
                new PasswordChangeRequest("password1", "newpassword2")
        );

        verify(user).changePassword("new-encoded-password");
        ArgumentCaptor<Collection<String>> keysCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(redisTemplate).delete(keysCaptor.capture());
        assertThat(keysCaptor.getValue()).containsExactlyInAnyOrder(
                JwtRedisKeys.refreshToken("refresh-id-1"),
                JwtRedisKeys.refreshToken("refresh-id-2")
        );
        verify(redisTemplate).delete(JwtRedisKeys.userRefreshTokens(USER_ID));
    }

    // 잘못된 현재 비밀번호 변경 거부 검증
    @Test
    void changePasswordRejectsInvalidCurrentPassword() {
        User user = createLocalUserMock();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(
                USER_ID,
                new PasswordChangeRequest("wrong-password", "newpassword2")
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_CURRENT_PASSWORD);
    }

    // 현재 기기 Refresh Token 폐기와 Access Token 블랙리스트 검증
    @Test
    void logoutConsumesRefreshTokenAndBlacklistsAccessToken() {
        prepareRedisOperations();
        Instant accessExpiresAt = Instant.now().plus(ACCESS_EXPIRATION);
        TokenClaims accessClaims = new TokenClaims(USER_ID, "access-id", accessExpiresAt);
        TokenClaims refreshClaims = new TokenClaims(
                USER_ID,
                "refresh-id",
                Instant.now().plus(REFRESH_EXPIRATION)
        );
        when(jwtProvider.parseRefreshToken("refresh-token")).thenReturn(refreshClaims);
        when(valueOperations.getAndDelete(JwtRedisKeys.refreshToken("refresh-id")))
                .thenReturn(USER_ID.toString());
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        authService.logout(USER_ID, accessClaims, new LogoutRequest("refresh-token"));

        verify(setOperations).remove(JwtRedisKeys.userRefreshTokens(USER_ID), "refresh-id");
        verify(valueOperations).set(
                eq(JwtRedisKeys.accessTokenBlacklist("access-id")),
                eq("true"),
                any(Duration.class)
        );
    }

    // 다른 사용자의 Refresh Token 로그아웃 거부 검증
    @Test
    void logoutRejectsRefreshTokenFromAnotherUser() {
        TokenClaims accessClaims = new TokenClaims(
                USER_ID,
                "access-id",
                Instant.now().plus(ACCESS_EXPIRATION)
        );
        TokenClaims refreshClaims = new TokenClaims(
                2L,
                "refresh-id",
                Instant.now().plus(REFRESH_EXPIRATION)
        );
        when(jwtProvider.parseRefreshToken("refresh-token")).thenReturn(refreshClaims);

        assertThatThrownBy(() -> authService.logout(
                USER_ID,
                accessClaims,
                new LogoutRequest("refresh-token")
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // 회원가입 요청 생성
    private SignUpRequest createSignUpRequest() {
        return new SignUpRequest(
                " USER@example.com ",
                "password1",
                " nickname ",
                "kr",
                Gender.FEMALE,
                Language.ENGLISH,
                Set.of(ServiceInterest.COURSE)
        );
    }

    // LOCAL 사용자 Mock 생성
    private User createLocalUserMock() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getProvider()).thenReturn(Provider.LOCAL);
        when(user.getPassword()).thenReturn("encoded-password");
        return user;
    }

    // Hibernate unique 제약조건 예외 생성
    private DataIntegrityViolationException createUniqueConstraintException(
            String constraintName
    ) {
        ConstraintViolationException cause = org.mockito.Mockito.mock(
                ConstraintViolationException.class
        );
        when(cause.getConstraintName()).thenReturn(constraintName);
        return new DataIntegrityViolationException("unique constraint", cause);
    }

    // Redis 문자열과 Set 연산 Mock 구성
    private void prepareRedisOperations() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        prepareSetOperations();
    }

    // Redis Set 연산 Mock 구성
    private void prepareSetOperations() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }
}
