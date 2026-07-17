package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

public abstract class AbstractOAuthJwksProvider {

    private static final String RSA_KEY_TYPE = "RSA";
    private static final String SIGNATURE_KEY_USE = "sig";
    private static final String RS256_ALGORITHM = "RS256";

    private final WebClient apiWebClient;
    private final String jwksPath;
    private final Duration cacheTtl;
    private final AuthErrorStatus invalidTokenStatus;
    private final AuthErrorStatus serverConnectionStatus;
    private volatile CachedJwks cachedJwks;

    // 공통 OAuth JWKS Provider 생성
    protected AbstractOAuthJwksProvider(
            WebClient apiWebClient,
            String jwksPath,
            Duration cacheTtl,
            AuthErrorStatus invalidTokenStatus,
            AuthErrorStatus serverConnectionStatus
    ) {
        this.apiWebClient = apiWebClient;
        this.jwksPath = jwksPath;
        this.cacheTtl = cacheTtl;
        this.invalidTokenStatus = invalidTokenStatus;
        this.serverConnectionStatus = serverConnectionStatus;
    }

    // key ID 기반 OAuth 서명 공개키 정보 조회 기능
    public OAuthJwk getSigningJwk(String keyId) {
        CachedJwks currentCache = getValidCache();

        if (currentCache != null) {
            OAuthJwk cachedKey = findSigningJwk(currentCache.keys(), keyId);
            if (cachedKey != null) {
                return cachedKey;
            }
        }

        OAuthJwk refreshedKey = findSigningJwk(refreshJwks(), keyId);
        if (refreshedKey == null) {
            throw new GeneralException(invalidTokenStatus);
        }

        return refreshedKey;
    }

    // 유효한 OAuth JWKS 캐시 조회 기능
    private CachedJwks getValidCache() {
        CachedJwks currentCache = cachedJwks;

        if (currentCache == null || !currentCache.expiresAt().isAfter(Instant.now())) {
            return null;
        }

        return currentCache;
    }

    // OAuth JWKS 조회 및 캐시 갱신 기능
    private synchronized List<OAuthJwk> refreshJwks() {
        try {
            OAuthJwksResponse response = apiWebClient.get()
                    .uri(jwksPath)
                    .retrieve()
                    .bodyToMono(OAuthJwksResponse.class)
                    .block();
            validateResponse(response);

            cachedJwks = new CachedJwks(
                    List.copyOf(response.keys()),
                    Instant.now().plus(cacheTtl)
            );
            return cachedJwks.keys();
        } catch (WebClientException exception) {
            throw new GeneralException(serverConnectionStatus, exception);
        }
    }

    // OAuth JWKS 응답 필수값 검증 기능
    private void validateResponse(OAuthJwksResponse response) {
        if (response == null || response.keys() == null || response.keys().isEmpty()) {
            throw new GeneralException(serverConnectionStatus);
        }
    }

    // OAuth 서명 용도 RSA 공개키 정보 검색 기능
    private OAuthJwk findSigningJwk(List<OAuthJwk> keys, String keyId) {
        return keys.stream()
                .filter(key -> keyId != null && keyId.equals(key.kid()))
                .filter(key -> RSA_KEY_TYPE.equals(key.kty()))
                .filter(key -> SIGNATURE_KEY_USE.equals(key.use()))
                .filter(key -> RS256_ALGORITHM.equals(key.alg()))
                .findFirst()
                .orElse(null);
    }

    private record CachedJwks(List<OAuthJwk> keys, Instant expiresAt) {
    }
}
