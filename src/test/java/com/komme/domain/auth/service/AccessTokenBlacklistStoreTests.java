package com.komme.domain.auth.service;

import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.jwt.JwtRedisKeys;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenBlacklistStoreTests {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // Access Token 블랙리스트 등록 검증
    @Test
    void blacklistStoresRemainingExpiration() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        TokenClaims claims = createClaims();

        new AccessTokenBlacklistStore(redisTemplate).blacklist(claims);

        verify(valueOperations).set(
                eq(JwtRedisKeys.accessTokenBlacklist("access-id")),
                eq("true"),
                any(Duration.class)
        );
    }

    // 블랙리스트 Access Token 거부 검증
    @Test
    void validateNotBlacklistedRejectsBlacklistedToken() {
        when(redisTemplate.hasKey(JwtRedisKeys.accessTokenBlacklist("access-id")))
                .thenReturn(true);

        assertThatThrownBy(() -> new AccessTokenBlacklistStore(redisTemplate)
                .validateNotBlacklisted(createClaims()))
                .extracting(exception -> ((com.komme.common.exception.GeneralException) exception)
                        .getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // Access Token 테스트 Claim 생성
    private TokenClaims createClaims() {
        return new TokenClaims(
                1L,
                "access-id",
                Instant.now().plus(Duration.ofHours(1))
        );
    }
}
