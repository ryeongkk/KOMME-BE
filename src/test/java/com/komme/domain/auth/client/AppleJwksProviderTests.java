package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.AppleProperties;

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

class AppleJwksProviderTests {

    private static final String KEY_ID = "apple-key-id";
    private static final String JWKS_JSON = """
            {
              "keys": [
                {
                  "kty": "RSA",
                  "kid": "apple-key-id",
                  "use": "sig",
                  "alg": "RS256",
                  "n": "modulus",
                  "e": "AQAB"
                }
              ]
            }
            """;

    // Apple JWKS TTL 캐시 재사용 검증
    @Test
    void getSigningJwkReusesCachedJwks() {
        AtomicInteger requestCount = new AtomicInteger();
        AppleJwksProvider provider = createProvider(createSuccessWebClient(requestCount));

        AppleJwk firstKey = provider.getSigningJwk(KEY_ID);
        AppleJwk secondKey = provider.getSigningJwk(KEY_ID);

        assertThat(firstKey).isEqualTo(secondKey);
        assertThat(requestCount).hasValue(1);
    }

    // Apple JWKS 서버 연결 실패 오류 변환 검증
    @Test
    void getSigningJwkMapsAppleServerFailure() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.BAD_GATEWAY).build()
                ))
                .build();
        AppleJwksProvider provider = createProvider(webClient);

        assertThatThrownBy(() -> provider.getSigningJwk(KEY_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.APPLE_SERVER_CONNECTION_FAILED);
    }

    // 테스트 Apple JWKS Provider 생성
    private AppleJwksProvider createProvider(WebClient webClient) {
        return new AppleJwksProvider(
                webClient,
                new AppleProperties("com.komme.app", Duration.ofHours(1))
        );
    }

    // 정상 Apple JWKS 응답 WebClient 생성
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
