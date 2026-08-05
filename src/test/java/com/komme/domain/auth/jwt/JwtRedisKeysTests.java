package com.komme.domain.auth.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRedisKeysTests {

    // Refresh Token Redis 키 생성 검증
    @Test
    void refreshTokenCreatesNamespacedKey() {
        assertThat(JwtRedisKeys.refreshToken("token-id"))
                .isEqualTo("auth:refresh-token:token-id");
    }

    // 사용자 Refresh Token 목록 Redis 키 생성 검증
    @Test
    void userRefreshTokensCreatesNamespacedKey() {
        assertThat(JwtRedisKeys.userRefreshTokens(1L))
                .isEqualTo("auth:user-refresh-tokens:1");
    }

    // Access Token 블랙리스트 Redis 키 생성 검증
    @Test
    void accessTokenBlacklistCreatesNamespacedKey() {
        assertThat(JwtRedisKeys.accessTokenBlacklist("token-id"))
                .isEqualTo("auth:access-token:blacklist:token-id");
    }
}
