package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.GoogleProperties;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Component
public class GoogleJwksProvider {

    private static final String GOOGLE_JWKS_PATH = "/oauth2/v3/certs";
    private static final String RSA_KEY_TYPE = "RSA";
    private static final String SIGNATURE_KEY_USE = "sig";
    private static final String RS256_ALGORITHM = "RS256";

    private final WebClient googleApiWebClient;
    private final GoogleProperties googleProperties;
    private volatile CachedJwks cachedJwks;

    // Google JWKS Provider 생성
    public GoogleJwksProvider(
            @Qualifier("googleApiWebClient") WebClient googleApiWebClient,
            GoogleProperties googleProperties
    ) {
        this.googleApiWebClient = googleApiWebClient;
        this.googleProperties = googleProperties;
    }

    // key ID 기반 Google 서명 공개키 정보 조회 기능
    public GoogleJwk getSigningJwk(String keyId) {
        CachedJwks currentCache = getValidCache();

        if (currentCache != null) {
            GoogleJwk cachedKey = findSigningJwk(currentCache.keys(), keyId);
            if (cachedKey != null) {
                return cachedKey;
            }
        }

        GoogleJwk refreshedKey = findSigningJwk(refreshJwks(), keyId);
        if (refreshedKey == null) {
            throw new GeneralException(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
        }

        return refreshedKey;
    }

    // 유효한 Google JWKS 캐시 조회 기능
    private CachedJwks getValidCache() {
        CachedJwks currentCache = cachedJwks;

        if (currentCache == null || !currentCache.expiresAt().isAfter(Instant.now())) {
            return null;
        }

        return currentCache;
    }

    // Google JWKS 조회 및 캐시 갱신 기능
    private synchronized List<GoogleJwk> refreshJwks() {
        try {
            GoogleJwksResponse response = googleApiWebClient.get()
                    .uri(GOOGLE_JWKS_PATH)
                    .retrieve()
                    .bodyToMono(GoogleJwksResponse.class)
                    .block();
            validateResponse(response);

            cachedJwks = new CachedJwks(
                    List.copyOf(response.keys()),
                    Instant.now().plus(googleProperties.getJwksCacheTtl())
            );
            return cachedJwks.keys();
        } catch (WebClientException exception) {
            throw new GeneralException(
                    AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED,
                    exception
            );
        }
    }

    // Google JWKS 응답 필수값 검증 기능
    private void validateResponse(GoogleJwksResponse response) {
        if (response == null || response.keys() == null || response.keys().isEmpty()) {
            throw new GeneralException(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
        }
    }

    // Google 서명 용도 RSA 공개키 정보 검색 기능
    private GoogleJwk findSigningJwk(List<GoogleJwk> keys, String keyId) {
        return keys.stream()
                .filter(key -> keyId != null && keyId.equals(key.kid()))
                .filter(key -> RSA_KEY_TYPE.equals(key.kty()))
                .filter(key -> SIGNATURE_KEY_USE.equals(key.use()))
                .filter(key -> RS256_ALGORITHM.equals(key.alg()))
                .findFirst()
                .orElse(null);
    }

    private record GoogleJwksResponse(List<GoogleJwk> keys) {
    }

    private record CachedJwks(List<GoogleJwk> keys, Instant expiresAt) {
    }
}
