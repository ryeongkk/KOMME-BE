package com.komme.domain.auth.jwt;

public final class JwtRedisKeys {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh-token:";
    private static final String USER_REFRESH_TOKENS_KEY_PREFIX = "auth:user-refresh-tokens:";
    private static final String ACCESS_TOKEN_BLACKLIST_KEY_PREFIX = "auth:access-token:blacklist:";

    // 인스턴스 생성 방지
    private JwtRedisKeys() {
    }

    // Refresh Token Redis 키 생성
    public static String refreshToken(String tokenId) {
        return REFRESH_TOKEN_KEY_PREFIX + tokenId;
    }

    // 사용자별 Refresh Token 목록 Redis 키 생성
    public static String userRefreshTokens(Long userId) {
        return USER_REFRESH_TOKENS_KEY_PREFIX + userId;
    }

    // Access Token 블랙리스트 Redis 키 생성
    public static String accessTokenBlacklist(String tokenId) {
        return ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + tokenId;
    }
}
