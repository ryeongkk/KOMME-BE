package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.GoogleProperties;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleJwksProviderTests {

    private static final String KEY_ID = "google-key-id";
    private static final String JWKS_JSON = """
            {
              "keys": [
                {
                  "kty": "RSA",
                  "kid": "google-key-id",
                  "use": "sig",
                  "alg": "RS256",
                  "n": "modulus",
                  "e": "AQAB"
                }
              ]
            }
            """;

    // Google JWKS TTL 캐시 재사용 검증
    @Test
    void getSigningJwkReusesCachedJwks() {
        AtomicInteger requestCount = new AtomicInteger();
        GoogleJwksProvider provider = createProvider(createSuccessWebClient(requestCount));

        OAuthJwk firstKey = provider.getSigningJwk(KEY_ID);
        OAuthJwk secondKey = provider.getSigningJwk(KEY_ID);

        assertThat(firstKey).isEqualTo(secondKey);
        assertThat(requestCount).hasValue(1);
    }

    // Google JWKS 서버 연결 실패 오류 변환 검증
    @Test
    void getSigningJwkMapsGoogleServerFailure() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.BAD_GATEWAY).build()
                ))
                .build();
        GoogleJwksProvider provider = createProvider(webClient);

        assertThatThrownBy(() -> provider.getSigningJwk(KEY_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
    }

    // 테스트 Google JWKS Provider 생성
    private GoogleJwksProvider createProvider(WebClient webClient) {
        return new GoogleJwksProvider(
                webClient,
                new GoogleProperties("google-client-id", Duration.ofHours(1))
        );
    }

    // 정상 Google JWKS 응답 WebClient 생성
    private WebClient createSuccessWebClient(AtomicInteger requestCount) {
        return WebClient.builder()
                .exchangeFunction(request -> {
                    requestCount.incrementAndGet();
                    ClientResponse response = ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(JWKS_JSON)
                            .build();
                    return Mono.just(response);
                })
                .build();
    }
}
