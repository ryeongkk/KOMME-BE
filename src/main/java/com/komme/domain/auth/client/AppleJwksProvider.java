package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.AppleProperties;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Component
public class AppleJwksProvider {

    private static final String APPLE_JWKS_PATH = "/auth/keys";
    private static final String RSA_KEY_TYPE = "RSA";
    private static final String SIGNATURE_KEY_USE = "sig";
    private static final String RS256_ALGORITHM = "RS256";

    private final WebClient appleApiWebClient;
    private final AppleProperties appleProperties;
    private volatile CachedJwks cachedJwks;

    // Apple JWKS Provider 생성
    public AppleJwksProvider(
            @Qualifier("appleApiWebClient") WebClient appleApiWebClient,
            AppleProperties appleProperties
    ) {
        this.appleApiWebClient = appleApiWebClient;
        this.appleProperties = appleProperties;
    }

    // key ID 기반 Apple 서명 공개키 정보 조회 기능
    public AppleJwk getSigningJwk(String keyId) {
        CachedJwks currentCache = getValidCache();

        if (currentCache != null) {
            AppleJwk cachedKey = findSigningJwk(currentCache.keys(), keyId);
            if (cachedKey != null) {
                return cachedKey;
            }
        }

        AppleJwk refreshedKey = findSigningJwk(refreshJwks(), keyId);
        if (refreshedKey == null) {
            throw new GeneralException(AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN);
        }

        return refreshedKey;
    }

    // 유효한 Apple JWKS 캐시 조회 기능
    private CachedJwks getValidCache() {
        CachedJwks currentCache = cachedJwks;

        if (currentCache == null || !currentCache.expiresAt().isAfter(Instant.now())) {
            return null;
        }

        return currentCache;
    }

    // Apple JWKS 조회 및 캐시 갱신 기능
    private synchronized List<AppleJwk> refreshJwks() {
        try {
            AppleJwksResponse response = appleApiWebClient.get()
                    .uri(APPLE_JWKS_PATH)
                    .retrieve()
                    .bodyToMono(AppleJwksResponse.class)
                    .block();
            validateResponse(response);

            cachedJwks = new CachedJwks(
                    List.copyOf(response.keys()),
                    Instant.now().plus(appleProperties.getJwksCacheTtl())
            );
            return cachedJwks.keys();
        } catch (WebClientException exception) {
            throw new GeneralException(
                    AuthErrorStatus.APPLE_SERVER_CONNECTION_FAILED,
                    exception
            );
        }
    }

    // Apple JWKS 응답 필수값 검증 기능
    private void validateResponse(AppleJwksResponse response) {
        if (response == null || response.keys() == null || response.keys().isEmpty()) {
            throw new GeneralException(AuthErrorStatus.APPLE_SERVER_CONNECTION_FAILED);
        }
    }

    // Apple 서명 용도 RSA 공개키 정보 검색 기능
    private AppleJwk findSigningJwk(List<AppleJwk> keys, String keyId) {
        return keys.stream()
                .filter(key -> keyId != null && keyId.equals(key.kid()))
                .filter(key -> RSA_KEY_TYPE.equals(key.kty()))
                .filter(key -> SIGNATURE_KEY_USE.equals(key.use()))
                .filter(key -> RS256_ALGORITHM.equals(key.alg()))
                .findFirst()
                .orElse(null);
    }

    private record AppleJwksResponse(List<AppleJwk> keys) {
    }

    private record CachedJwks(List<AppleJwk> keys, Instant expiresAt) {
    }
}
