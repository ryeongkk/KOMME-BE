package com.komme.domain.auth.service;

import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.jwt.JwtProvider;
import com.komme.domain.auth.jwt.JwtProvider.IssuedToken;
import com.komme.domain.auth.jwt.JwtRedisKeys;
import com.komme.domain.user.service.UserReader;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTests {

    private static final Long USER_ID = 1L;
    private static final Duration ACCESS_EXPIRATION = Duration.ofHours(1);
    private static final Duration REFRESH_EXPIRATION = Duration.ofDays(14);

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TermsAgreementService termsAgreementService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    // 로그인 토큰 발급과 Refresh Token 저장 검증
    @Test
    void issueLoginTokensIssuesAndStoresTokens() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(jwtProvider.issueAccessToken(USER_ID))
                .thenReturn(new IssuedToken("access-token", "access-id", ACCESS_EXPIRATION));
        when(jwtProvider.issueRefreshToken(USER_ID))
                .thenReturn(new IssuedToken("refresh-token", "refresh-id", REFRESH_EXPIRATION));
        UserReader userReader = org.mockito.Mockito.mock(UserReader.class);
        AuthTokenService authTokenService = new AuthTokenService(
                jwtProvider,
                new RefreshTokenStore(redisTemplate, userReader),
                termsAgreementService
        );

        LoginResponse response = authTokenService.issueLoginTokens(USER_ID);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(valueOperations).set(
                JwtRedisKeys.refreshToken("refresh-id"),
                USER_ID.toString(),
                REFRESH_EXPIRATION
        );
        verify(setOperations).add(JwtRedisKeys.userRefreshTokens(USER_ID), "refresh-id");
        verify(redisTemplate).expire(
                JwtRedisKeys.userRefreshTokens(USER_ID),
                REFRESH_EXPIRATION
        );
    }
}
